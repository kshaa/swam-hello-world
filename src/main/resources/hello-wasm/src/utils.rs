use std::mem::transmute;
use std::convert::TryFrom;

// Unsafely write a Rust UTF-8 string into a static constant buffer
pub unsafe fn str_to_buffer<const SIZE: usize>(buffer: &mut [u8; SIZE], text: &str) {
    // Write UTF-8 string byte size as a little-endian 4-byte int to buffer
    let text_size: [u8; 4] = transmute((i32::try_from(text.len()).unwrap()).to_be());
    for (i, byte) in text_size.iter().enumerate() {
        let pointer_math = (buffer as *mut u8 as usize) + 3 - i;
        let pointer = pointer_math as *mut u8;
        *pointer = byte.clone();
    }

    // Write UTF-8 string content bytes to buffer
    for (i, byte) in text.as_bytes().iter().enumerate() {
        let pointer_math = (buffer as *mut u8 as usize) + 4 + i;
        let pointer = pointer_math as *mut u8;
        *pointer = byte.clone();
    }
}

pub unsafe fn little_endian_int<const SIZE: usize>(buffer: &[u8; SIZE]) -> u32 {
    // Read UTF-8 string byte size as a little-endian 4-byte int from buffer
    let base = (buffer as *const u8) as usize;
 
    ((*((base + 3) as *const u8) as u32) << 24) +
    ((*((base + 2) as *const u8) as u32) << 16) +
    ((*((base + 1) as *const u8) as u32) << 8) +
    ((*((base + 0) as *const u8) as u32) << 0)
}

// Unsafely read a Rust UTF-8 string from a static constant buffer
pub unsafe fn str_from_buffer<const SIZE: usize>(buffer: &[u8; SIZE]) -> String {
    // Read text size from buffer
    let text_size = little_endian_int(buffer);
    // Read text contents from buffer
    let mut byte_vector: Vec<u8> = vec!();
    for i in 0..text_size {
        byte_vector.push(*((((buffer as *const u8) as usize) + 4 + i as usize) as *const u8));
    } 
    // Read text string content bytes from buffer
    return String::from_utf8(byte_vector).unwrap();
}

// Reverse string
pub fn reversed(text: String) -> String {
    text.chars().rev().collect::<String>()
}

#[cfg(test)]
mod tests {
    #[test]
    fn reader_test() {
        let text = String::from("A");
        let bytes: [u8; 5] = [1, 0, 0, 0, 65];

        assert_eq!(unsafe { super::str_from_buffer(&bytes) }, text);
    }

    #[test]
    fn int_test() {
        let n: u32 = 1;
        let bytes: [u8; 4] = [1, 0, 0, 0];

        assert_eq!(unsafe { super::little_endian_int(&bytes) }, n);
    }

    #[test]
    fn reverse_test() {
        assert_eq!(super::reversed(String::from("potat")), String::from("tatop"));
    }
}
