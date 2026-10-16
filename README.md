# Marvellous FTP: Multi-Client File Transfer in Java

A console-based file transfer system built with **Java TCP sockets** and **multithreading**. A server shares the files in its working directory, and any number of clients can connect at the same time to list, inspect, download, upload, rename and delete files.

Built as a learning project to practice socket programming, stream handling, a custom text/binary protocol and thread-per-client server design.

---

## Table of Contents

1. [Features](#features)
2. [Project Structure](#project-structure)
3. [Requirements](#requirements)
4. [Getting Started](#getting-started)
5. [Command Reference](#command-reference)
6. [Example Session](#example-session)
7. [How It Works](#how-it-works)
8. [Communication Protocol](#communication-protocol)
9. [Design Notes](#design-notes)
10. [Known Limitations](#known-limitations)
11. [Future Improvements](#future-improvements)
12. [Contributing](#contributing)
13. [License](#license)

---

## Features

- Multi-client server: each client is handled on its own thread
- Upload (`PUT`) and download (`GET`) of any file type, text or binary
- File inspection: existence, size, readability and writability
- Remote file management: `LIST`, `RENAME`, `DELETE`
- Input validation on both client and server with usage hints
- Chunked transfer (1 KB buffer) so large files do not need to fit in memory
- Transfer verification: the byte count received is compared with the announced file size
- Zero external dependencies, using only the Java standard library

---

## Project Structure

```
.
├── program934.java   # Server: accepts connections, spawns a thread per client
├── program935.java   # Client: interactive console application
├── README.md
└── .gitignore
```

| File | Class | Role |
|------|-------|------|
| `program934.java` | `program934` | TCP server listening on port **9000** |
| `program935.java` | `program935` | TCP client connecting to **127.0.0.1:9000** |

---

## Requirements

- **JDK 8 or later** (the server uses a lambda expression for its threads)
- A terminal or command prompt
- Port **9000** free on the machine running the server

Check your Java version:

```bash
java -version
javac -version
```

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

### 2. Compile

```bash
javac program934.java program935.java
```

### 3. Start the server

Run the server from the folder whose files you want to share. All file operations are relative to the server's working directory.

```bash
java program934
```

Expected output:

```
-------------------------------------
----- Marvellous Server Started -----
-------------------------------------
Server is waiting for client request
```

### 4. Start one or more clients

Open a new terminal for each client:

```bash
java program935
```

---

## Command Reference

Commands are case-insensitive for the command word (`get`, `GET` and `Get` all work). File names are case-sensitive on most operating systems.

| Command | Arguments | Description |
|---------|-----------|-------------|
| `LIST` | none | Lists the files present on the server |
| `EXISTS` | `<FileName>` | Tells whether the file exists on the server |
| `INFO` | `<FileName>` | Shows file name, size, readable and writable flags |
| `SIZE` | `<FileName>` | Shows the file size in bytes |
| `GET` | `<FileName>` | Downloads the file; saved locally as `Download_<FileName>` |
| `PUT` | `<FileName>` | Uploads a local file to the server |
| `DELETE` | `<FileName>` | Deletes the file on the server |
| `RENAME` | `<OldFileName> <NewFileName>` | Renames a file on the server |
| `QUIT` | none | Closes the connection |

If you give the wrong number of arguments, the program prints the correct usage, for example `Usage : GET <FileName>`.

---

## Example Session

```
Connected to Marvellous Server
Enter command :
SIZE notes.txt
-------------------------------------
File size is : 1284 bytes
-------------------------------------

Enter command :
GET notes.txt
File size is : 1284
Download completed succesfully

Enter command :
PUT report.pdf
File uploaded succesfully

Enter command :
QUIT
Thank you for using Marvellous FTP Server
Disconneced from server
```

---

## How It Works

### Server (`program934`)

1. Creates a `ServerSocket` on port 9000.
2. Loops forever on `accept()`, waiting for clients.
3. For each accepted `Socket`, starts a new `Thread` running `HandleClientRequest`.
4. Each handler sends a welcome message, then repeatedly reads a command, splits it on spaces, and dispatches on the first word.
5. A `ClientCount` counter tracks connected clients for log messages.

### Client (`program935`)

1. Opens a `Socket` to the server and prints the welcome message.
2. Shows the command menu and reads a line from the user.
3. Validates the argument count locally before sending anything.
4. Sends the command, then reads the server's response (and file data for `GET`).
5. On `QUIT`, notifies the server, breaks out of the loop and closes the streams.

### Streams used

| Stream | Purpose |
|--------|---------|
| `DataInputStream` / `DataOutputStream` | Strings (`readUTF` / `writeUTF`), file sizes (`readLong` / `writeLong`) and raw bytes |
| `FileInputStream` / `FileOutputStream` | Reading and writing files on disk |

---

## Communication Protocol

Every request is a single UTF string such as `GET notes.txt`. The server replies depending on the command.

### Simple commands (`EXISTS`, `INFO`, `SIZE`, `RENAME`, `DELETE`, `LIST`, `QUIT`)

```
Client  ->  "SIZE notes.txt"
Server  ->  "File size is : 1284 bytes"
```

### Download: `GET`

```
Client  ->  "GET notes.txt"
Server  ->  "File found"  |  "FILE_NOT_FOUND"
Server  ->  <long: file size>            (only if found)
Server  ->  <file bytes in 1 KB chunks>
```

The client reads exactly `file size` bytes, writes them to `Download_<FileName>`, and reports success only if the count matches.

### Upload: `PUT`

```
Client  ->  "PUT report.pdf"
Server  ->  "READY"
Client  ->  <long: file size>
Client  ->  <file bytes in 1 KB chunks>
Server  ->  "File uploaded succesfully"  |  "File uploaded failed"
```

The client checks locally that the file exists before sending the command, so the server is never asked to receive a missing file.

---

## Design Notes

- **Thread per client** keeps the code simple and lets clients work independently. One slow transfer does not block others.
- **Length-prefixed transfers**: sending the size first lets the receiver know exactly when the file ends, with no end-of-file marker needed.
- **Bounded reads**: the receive loop never reads past the announced size, which keeps the command stream in sync after a transfer.
- **Fixed 1024-byte buffer** keeps memory use constant regardless of file size.

---

## Known Limitations

- File names containing spaces are not supported because commands are split on spaces.
- No authentication or encryption. Anyone who can reach the port can read, overwrite and delete files, so use it only on a trusted network.
- No path validation. A name like `../secret.txt` could reach outside the intended folder (path traversal).
- `PUT` overwrites an existing server file of the same name without asking.
- The server host and port are hard-coded (`127.0.0.1:9000` on the client).
- `ClientCount` is a plain static `int` updated from several threads, so it is not thread-safe.
- A client that disconnects abruptly is handled only through the generic exception catch.

---

## Future Improvements

- [ ] Support file names with spaces (quoting or a length-prefixed command format)
- [ ] Restrict access to a dedicated shared directory and reject `..` paths
- [ ] Username and password login
- [ ] TLS encryption with `SSLServerSocket`
- [ ] Thread pool (`ExecutorService`) instead of unbounded threads
- [ ] `AtomicInteger` for the client counter
- [ ] Configurable host and port through command-line arguments
- [ ] Transfer progress bar
- [ ] Overwrite confirmation on `PUT`
- [ ] Unit and integration tests

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-change`
3. Commit your changes: `git commit -m "Describe your change"`
4. Push the branch: `git push origin feature/my-change`
5. Open a Pull Request

