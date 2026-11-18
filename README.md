# 📄 Telegram PDF-to-Image Bot (Pure Java, Zero Framework Bloat)

A lightweight, high-performance Telegram bot built entirely in plain Java that takes multi-page PDF documents, renders them into crystal-clear PNG images locally, and delivers them as neat, swipeable photo albums directly in your chat.

No Spring Boot. No heavy ORMs. No Gson. Just raw Java, HTTP networking, and first-principles engineering.

---

## 💡 Why Did I Build This?

Most modern bot tutorials rely on heavy frameworks or pre-packaged wrappers that hide how things actually work behind the scenes. I wanted to build something different: a production-grade Telegram bot from the metal up.

By avoiding heavy third-party libraries, I forced myself to dive deep into HTTP multipart protocols, socket streams, and low-level JSON parsing. It turned into an incredible masterclass in how APIs actually communicate under the hood.

---

## 🚀 What It Does

1. **Receives Documents:** Listens for PDF uploads from users via Telegram long-polling (`getUpdates`).
2. **Local Rendering:** Uses Apache PDFBox to cleanly render every page of the PDF into individual high-res PNG images.
3. **Smart Album Batching:** Bundles images into batches of up to 10 and delivers them using Telegram's `sendMediaGroup` API—completely eliminating chat spam and dodging API rate limits (`400/502` errors).
4. **Zero-Dependency Parsing:** Extracts critical metadata (like Telegram `file_id`s) using a custom depth-tracking string parser instead of pulling in massive serialization libraries.

---

## 🛠️ Under the Hood: The Engineering Highlights

### 1. Writing a Custom JSON Parser (Instead of Gson)
Instead of pulling in a 250+ KB serialization library like Gson, I wrote a lightweight, custom string parser (`SimpleJsonParser`) featuring **brace-depth tracking**.
* *Why it matters:* When Telegram returns responses containing nested objects (like a document block containing both a thumbnail ID and a main file ID), a blind parser gets confused. Depth-tracking allows the code to precisely count `{` and `}` scopes and extract the exact `file_id` needed without breaking a sweat.

---

## 🧰 Tech Stack

* **Language:** Pure Java (JDK 11+)
* **PDF Rendering:** Apache PDFBox 3.x
* **Networking:** `HttpURLConnection` (Java Standard Library)
* **IDE:** IntelliJ IDEA

