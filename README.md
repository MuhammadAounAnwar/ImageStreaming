# **Image Streamer App**

This Android project follows the **MVVM (Model-View-ViewModel)** architecture and **Clean Architecture** principles to process and upload images efficiently. The app captures frames from the camera, processes them, stores them locally, and uploads them to a server in batches. The app also ensures that no frames are left behind using a **foreground service** and an **upload scheduler**.

The app uses **file.io** as the server for uploading files. **file.io** provides a simple and secure way to upload files and get a temporary URL for accessing them. It is a file-sharing service where files can be uploaded, stored, and shared with a unique URL, which expires after the first download or after a set time period.

## **Project Structure**

The project is divided into three layers:

### 1. **Data Layer**

The **data layer** is responsible for data fetching, storage, and persistence. It interacts with both remote and local data sources.

- **API Client Classes**: Handle interaction with external services via **Retrofit** or **OkHttp**.
- **DAO Classes**: Handle local storage interactions via **Room Database** or **SQLite**.
- **Repositories**: Manage data from remote and local sources and provide a unified data access API.

In this project, the **data layer** is responsible for uploading images to the server and storing them locally in a **Room database**.

### 2. **Domain Layer**

The **domain layer** focuses on the application's business logic and defines operations that perform work on entities.

- **Entities**: Represent business objects used across the system.
- **Use Cases**: Contain the business rules and perform operations on entities.
- **Repositories (Abstractions)**: Define operations for fetching and modifying data without implementation details.

In this project, the **domain layer** is used for processing the frames captured by the camera, including analyzing and uploading them to the server.

### 3. **Presentation Layer**

The **presentation layer** is responsible for the UI components, interacting with the **view models**, and presenting the processed data to the user.

- **UI**: Includes camera view and image upload feedback.
- **ViewModel**: Binds data and communicates with use cases to perform operations on the data layer.

## **Functional Overview**

1. **Permissions**: The app requests the necessary permissions for **camera** and **notifications** based on the Android version.
2. **Frame Capture**: After permissions are granted, the app initializes the **camera** and presents the camera view to the user.
3. **Frame Processing**: Each captured frame is processed by the **ViewModel**, which performs the following steps:
    - Convert the frame to a **Bitmap**.
    - Modify its dimensions.
    - Apply **compression** to the image.
    - Store the compressed frame as a **byteArray** in the **Room database**.

4. **Single Source of Truth**: The app maintains the consistency of data by always fetching images from the **Room database**, ensuring that no frames are missed for upload.

5. **Uploading Frames**:
    - A **foreground service** is used to upload frames to the server.
    - Whenever a frame is added to the database with a "pending" status, the **foreground service** is triggered to upload it.
    - The upload is handled in batches of 3 to optimize resource usage and minimize the impact on device performance.
    - Once an image is successfully uploaded, its status is updated to "uploaded".
    - The **file.io** server is used for uploading the frames, where each file is stored temporarily and can be accessed via a unique URL.

6. **Upload Scheduler**:
    - A **UploadFramesScheduler** is used to ensure that no frames are left to upload after the foreground service stops.
    - The scheduler checks for any pending frames and initiates the upload process.
    - For integrating custom parameters into the **WorkManager** schedule, a **custom factory pattern** is used. This pattern allows the injection of specific parameters into the **OneTimeWorkRequest** or **PeriodicWorkRequest** before scheduling the work. The custom factory creates requests with additional configurations, improving flexibility and customization in task scheduling.

7. **Pause and Resume Upload**:
    - The app includes functionality to **pause** and **resume** image uploads only if there are uploads currently in progress.
    - To achieve this functionality efficiently, the app uses a **mutex** to lock the upload process. This approach avoids using **LiveData** or **Flow**, as those involve polling and can lead to unnecessary resource consumption.
    - The **mutex** ensures that only one upload operation is in progress at any given time, allowing the upload process to be paused and resumed without causing performance issues.

8. **Offline Upload Handling**:
    - If the device is **offline** or there is no internet connection, the app will **not attempt to upload any frames**.
    - Instead, the frames will be stored in the **Room database** with a "pending" status.
    - As soon as the device regains **internet connectivity**, the app will automatically begin uploading the **pending frames**.
    - This ensures that the app doesn’t waste resources by attempting uploads without connectivity and ensures that frames are uploaded as soon as possible once the internet is available.

## **Key Features**

- **Camera Integration**: Capture frames from the camera in real-time.
- **Image Processing**: Modify dimensions, compress, and store frames.
- **Foreground Service**: Efficient image upload with background processing.
- **Batch Uploading**: Upload frames in batches to optimize resource usage.
- **Upload Scheduler**: Automatically ensures frames are uploaded, even after the foreground service is destroyed.
- **File.io Integration**: Uses **file.io** to upload images and obtain temporary URLs for each uploaded file.
- **Custom Factory Pattern for WorkManager**: Custom parameters can be passed to **WorkManager** using a factory pattern, enhancing the flexibility of task scheduling.
- **Pause and Resume Upload**: Uploads can be **paused** and **resumed** efficiently using a **mutex**, preventing unnecessary resource consumption from polling.
- **Offline Upload Handling**: Frames are stored in the **Room database** when offline and uploaded once the device has internet connectivity.

## **Dependencies**

- **Retrofit** for network requests.
- **Room Database** for local storage.
- **Coroutine** for asynchronous operations.
- **WorkManager** for scheduling tasks.
- **Hilt** for **Dependency Injection**.

## **How It Works**

1. **User Interface**:
    - User grants **camera** and **notification** permissions.
    - The **camera view** is displayed, and frames are captured and processed.
    
2. **Frame Processing**:
    - The **ViewModel** processes the frames by converting them to a **Bitmap**, modifying dimensions, compressing, and storing them in the **Room database**.

3. **Image Upload**:
    - The frames are uploaded to the server using a **foreground service**. Once the upload is successful, the frame's status is updated in the database. The files are uploaded to **file.io**, a file-sharing service, and can be accessed via the provided URL.

4. **Scheduler**:
    - If there are pending frames after the service ends, the **UploadFramesScheduler** ensures the frames are uploaded. The scheduler uses a **custom factory pattern** to include custom parameters and configure the work requests.

5. **Pause/Resume Functionality**:
    - The upload can be **paused** and **resumed** only when uploads are in progress. This is achieved using a **mutex**, which prevents the overhead of polling by **LiveData** or **Flow**.

6. **Offline Upload Handling**:
    - If the device is offline, frames are stored in the **Room database** with a "pending" status.
    - Once the device regains internet connectivity, the app automatically begins uploading the pending frames.

## **file.io Integration**

**file.io** is a simple file-sharing service that allows users to upload files and retrieve temporary URLs for access. When a file is uploaded to **file.io**, it is stored temporarily and can be accessed via a unique URL. Once the file is downloaded, the URL expires, ensuring that files are only accessible for a limited time. This integration is used in the app to upload processed image frames and provide access to the images through the generated URLs.

## **Testing with Camera Images**

If you want to manually test the image capture and upload functionality using **camera images**, follow these steps:

1. Open **MainActivity**.
2. Uncomment the following code snippet:

```kotlin
/*MainScreen(
    modifier = Modifier.padding(innerPadding),
    viewModel = viewModel
)*/
```
## **Testing with Camera Images**

By uncommenting the code, you will be able to capture images by clicking the button, and those images will be uploaded to the server. This allows you to test the same functionality of capturing, processing, and uploading images as the app runs normally.


## **Conclusion**

This project demonstrates an efficient approach to capturing, processing, storing, and uploading images in batches using modern Android development practices. The combination of **MVVM**, **Clean Architecture**, **Hilt** for **Dependency Injection**, and optimized **foreground services** ensures a responsive and reliable application. Additionally, the integration with **file.io** for temporary file storage and URL sharing makes this app a robust solution for image streaming and uploading.
