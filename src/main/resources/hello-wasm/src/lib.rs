mod utils;

use wasm_bindgen::prelude::*;

// Use a static mutable buffer for passing complex data between WASM and host
const WASM_MEMORY_BUFFER_SIZE: usize = 256;
static mut WASM_MEMORY_BUFFER: [u8; WASM_MEMORY_BUFFER_SIZE] = [0; WASM_MEMORY_BUFFER_SIZE];

// Fill buffer with sample text
#[wasm_bindgen]
pub fn fill_buffer() {
    let text: String = String::from("Ž! A UTF-8 symbol with a latvian diacritical mark.");
    
    unsafe { utils::str_to_buffer(&mut WASM_MEMORY_BUFFER, &text); }
}

// Parse buffer, reverse characters, fill back in
#[wasm_bindgen]
pub fn parse_and_reverse_buffer() {
    let text: String;
    unsafe { text = utils::str_from_buffer(&WASM_MEMORY_BUFFER); } 
    let reversed = utils::reversed(text);
    unsafe { utils::str_to_buffer(&mut WASM_MEMORY_BUFFER, &reversed); }
}

// Return buffer address
#[wasm_bindgen]
pub fn get_buffer() -> *const u8 {
    unsafe {
        return WASM_MEMORY_BUFFER.as_ptr();
    }
}
