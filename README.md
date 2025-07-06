## Gardrops Image Upload Session Microservices (Spring Boot) Assignment

This project is a **Spring Boot microservices-based pipeline** for uploading and processing images.  
It consists of two services: a **ImageUploadApi** for session management & uploads, and an **ImageProcessingApi** for image processing.

##  Architecture

### 1️⃣ **ImageUploadApi**
- Port: **8080**
- Accepts image upload requests from clients.
- Enforces:
  - Maximum 10 images per session
  - Rate limit: 10 requests/minute per IP
- Stores uploaded images temporarily on disk:  
  `/tmp/uploads/{sessionId}/{imageId}.jpg`
- Maintains session & image IDs in Redis.
- Sends the uploaded image to the Internal API for processing.

### 2️⃣ **ImageProcessingApi**
- Port: **8081**
- Processes the image sent by Upload API:
  - Resizes to **≤ 720x1280px**
  - Compresses as **JPEG (90% quality)**
- Saves the processed image at the given `destinationFilePath`.

- 
## 📦 Technologies

✅ **Java 24**  
✅ **Spring Boot 3.5.3**  
✅ Spring Web, Spring Data Redis, Spring Validation  
✅ Redis (for session data & rate limiting)  
✅ javax.imageio (for image processing)
