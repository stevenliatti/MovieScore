use std::env;
use std::fs::File;
use std::io::{Write, BufReader, BufRead};
use serde::{Serialize, Deserialize};

#[derive(Serialize, Deserialize, Debug)]
struct Movie {
    id: usize,
    original_title: String,
    popularity: f64,
    video: bool,
    adult: bool,
}

fn make_url(api_key: &String, id: usize) -> String {
    return format!("https://api.themoviedb.org/3/movie/{}?api_key={}&language=en-US&append_to_response=credits%2Ckeywords%2Csimilar", id, api_key);
}

fn make_all_ids(input_file: &String) -> Vec<usize> {
    let file = File::open(input_file).unwrap();
    let reader = BufReader::new(file);
    let mut all_ids = vec![];
    for line in reader.lines() {
        let movie: Movie = serde_json::from_str(&line.unwrap()).unwrap();
        all_ids.push(movie.id);
    }
    return all_ids;
}

fn make_this_ids(machines: usize, machine_id: usize, all_ids: &Vec<usize>) -> Vec<usize> {
    let mut this_ids = vec![];
    let mut count: usize = machine_id;
    for (i, id) in all_ids.iter().enumerate() {
        if i == count {
            this_ids.push(id.clone());
            count = count + machines;
        }
    }
    return this_ids;
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let args: Vec<String> = env::args().collect();
    let api_key = &args[1];
    let input_file = &args[2];
    let output_file = &args[3];
    let machines = if args.len() > 4 { args[4].parse().unwrap() } else { 1 };
    let machine_id = if args.len() > 5 { args[5].parse().unwrap() } else { 1 };

    let all_ids = make_all_ids(input_file);
    let ids = if args.len() > 5 { make_this_ids(machines, machine_id, &all_ids) } else { all_ids };
    let mut output = File::create(output_file)?;

    for id in ids {
        let response = reqwest::get(&make_url(api_key, id)).await?.text().await?;
        write!(output, "{}\n", response)?;
    }
    Ok(())
}
