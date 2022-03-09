# Swam hello world

A hello world example of the [`swam`](https://github.com/satabin/swam) WebAssembly engine executing compiled Rust.  
This code has unsafe written all over it, but it's all in the name of science.  

## Tests
1. `WAT` execution
    - Scala w/ `swam` compiles a plaintext source code file `.wat` into `.wasm` in-memory
    - Scala passes an integer to this `WASM` program and prints out the resulting/returned integer
2. `WASM` execution
   - Scala w/ `swam` uses an already compiled binary Rust `WASM` program
   - Scala reads some strings out
   - Scala writes some strings in
   - Rust reads some strings in
   - Rust writes some strings out

## Results
```
1. Compiling plaintext wasm and running it
- WASM returned fibonacci calculation for 13, result: 377
2. Running Rust compiled to binary WASM
- Rust returned a buffer at address: 0x1010E8
- Rust filled and returned a buffer with contents: 'Ž! A UTF-8 symbol with a latvian diacritical mark.'
- Scala filled the buffer 0x1010E8 in memory 'memory' with contents: 'Ž! Once again a UTF-8 symbol with a latvian diacritical mark.'
- Rust parsed and reversed buffer contents: '.kram lacitircaid naivtal a htiw lobmys 8-FTU A !Ž'
```

## File contents
- [`Hello.scala`](./src/main/scala/example/Hello.scala) - Scala code which conditionally compiles & then runs `WASM` code using the `swam` engine  
- [`lib.rs`](./src/main/resources/hello-wasm/src/lib.rs) - Rust code which compiles to WASM, which reads and writes strings using an intermediary byte buffer  
- [`utils.rs`](./src/main/resources/hello-wasm/src/utils.rs) - Very ugly Rust code to read/write buffer contents  
- [`fibo.wat`](./src/main/resources/fibo/fibo.wat) - Plaintext WASM source code which implements the fibonacci sequence   
  