// TMDb movies crawler
// Steven Liatti & Jeremy Favre

use std::env;
use std::fs::File;
use std::io::{Write, BufReader, BufRead};
use std::time::Instant;
use serde::{Serialize, Deserialize};
use std::sync::mpsc::channel;
use std::thread;

#[derive(Serialize, Deserialize, Debug)]
struct Movie {
    id: usize,
    original_title: String,
    popularity: f64,
    video: bool,
    adult: bool,
}

fn make_url(api_key: &String, id: usize) -> String {
    return format!("https://api.themoviedb.org/3/movie/{}?api_key={}&language=en-US&append_to_response=credits%2Ckeywords%2Csimilar%2Crecommendations", id, api_key);
}

// Extract each id from the daily export file from TMDb API
// and return an ids vector
fn make_ids(input_file: &String) -> Vec<usize> {
    let file = File::open(input_file).unwrap();
    let reader = BufReader::new(file);
    let mut all_ids = vec![];
    for line in reader.lines() {
        let movie: Movie = serde_json::from_str(&line.unwrap()).unwrap();
        all_ids.push(movie.id);
    }
    return all_ids;
}

// Divide ids vector in multiple sub vectors, one for each thread
// Jump from multiple of threads to another to take current id
fn make_thread_ids(machines: usize, machine_id: usize, all_ids: &Vec<usize>) -> Vec<usize> {
    let mut thread_ids = vec![];
    let mut count: usize = machine_id;
    for (i, id) in all_ids.iter().enumerate() {
        if i == count {
            thread_ids.push(id.clone());
            count = count + machines;
        }
    }
    return thread_ids;
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Start timer
    let now = Instant::now();

    // Args management
    let args: Vec<String> = env::args().collect();
    let api_key = &args[1];
    let input_file = &args[2];
    let output_file = &args[3];
    let threads = &args[4].parse().unwrap();

    // Extract all ids from TMDb daily export file
    let ids = make_ids(input_file);

    // Threads and channel management
    let done = String::from("done");
    let (crawlers, writer) = channel();

    let handles = (0..*threads).into_iter().map(|i| {
        let api_key_clone = api_key.clone();
        let done_clone = done.clone();
        let crawler = crawlers.clone();
        // In each thread, make requests from own thread_ids and send it to writer
        let thread_ids = make_thread_ids(*threads, i, &ids);
        let handle = thread::spawn(move || {
            for id in thread_ids {
                let response = reqwest::blocking::get(&make_url(&api_key_clone, id)).unwrap().text().unwrap();
                crawler.send(response).unwrap();
            }
            // When done, send "done" message
            crawler.send(done_clone).unwrap();
        });
        handle
    }).collect::<Vec<thread::JoinHandle<_>>>();

    // Join all threads
    for handle in handles {
        handle.join().unwrap()
    }

    // Write all responses in a file
    let mut output = File::create(output_file).unwrap();
    let mut counter = 0;
    for message in writer {
        if message == done { counter = counter + 1; }
        else { write!( output, "{}\n", message).unwrap(); }
        if &counter == threads { break; }
    }

    println!("Done in {} seconds", now.elapsed().as_secs());
    Ok(())
}
