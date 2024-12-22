# FoodRescue

FoodRescue is an Android application designed to reduce food wastage by allowing users to share leftover food with people in need nearby. Users can upload details about available food, and others can view these posts and collect the food.

## Features

- **User Authentication**: Sign up and log in securely using Firebase Authentication.
- **Role-Based Navigation**: Donors and recipients have role-specific interfaces for posting and viewing food.
- **Food Posting**: Donors can upload food details, including name, freshness, servings, and location.
- **Food Listing**: Recipients can browse available food posts.
- **Map Integration**: View the location of food posts on a map for easy navigation.
- **Timer Feature**: Food posts expire after a specified duration to ensure freshness.

---

## Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Firebase account with Firestore database setup
- Google Maps API key (for location services)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/FoodRescue.git
   ```
2. Open the project in Android Studio.
3. Configure Firebase:
   - Add your `google-services.json` file to the `app` directory.
   - Enable Firebase Authentication and Firestore database.
4. Configure Google Maps:
   - Add your Google Maps API key to the `AndroidManifest.xml` file.
5. Sync Gradle and run the project on an emulator or a physical device.

---

## Project Structure

```
FoodRescue/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yourpackage/
│   │   │   │   ├── MainActivity.kt        # Core logic and navigation
│   │   │   │   ├── ComposableScreens.kt  # UI components for screens
│   │   │   │   ├── FoodRescueApplication.kt  # Firebase initialization
│   │   │   ├── res/                      # UI resources (layouts, drawables, etc.)
│   │   ├── manifest/                     # AndroidManifest.xml
├── build.gradle
├── README.md
```

---

## Screenshots

### 1. Login Screen
![image](https://github.com/user-attachments/assets/e81a34f4-1eea-48ee-8dc5-fdc8a97878f5)

### 3. Role Selection Screen
![image](https://github.com/user-attachments/assets/fd3ece53-3f28-484b-a843-ccd8b672a8b7)

### 2. Food Posting Screen
![image](https://github.com/user-attachments/assets/77976e82-3799-4618-ba98-67f2d35938bc)


### 3. Food List
![image](https://github.com/user-attachments/assets/f04e8c28-f023-4d7f-896b-e79ce8e4e297)


### 4. Food Details Dialog Box
![image](https://github.com/user-attachments/assets/dbeff4ef-00d3-44ec-bef4-10ad0d44ffdf)


---

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **Location Services**: Google Maps API

---

## Firebase Setup

### Firestore Rules
Set the Firestore rules to ensure security:
```javascript
service cloud.firestore {
  match /databases/{database}/documents {

    // Allow all authenticated users to read user profiles
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // Food post rules
    match /foodPosts/{foodPostId} {
      allow read: if request.auth != null;

      // Allow creation by any authenticated user
      allow create: if request.auth != null;

      // Allow updates if donor or recipient modifies the post
      allow update: if request.auth != null && (
        request.auth.uid == resource.data.donorId || (
          resource.data.status == 'available' && 
          request.resource.data.status == 'accepted' &&
          request.resource.data.recipientId == request.auth.uid
        )
      );

      // Allow deletion only by the donor
      allow delete: if request.auth != null && request.auth.uid == resource.data.donorId;
    }
  }
}
```
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /foodPosts/{document} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Database Structure

- **Collection**: `foodPosts`
  - `id`: Document ID (set as `donorId` for simplicity)
  - `userId`: Recipient’s user ID
  - `donorId`: Donor’s user ID
  - `foodName`: Name of the food
  - `freshness`: Food freshness
  - `location`: GeoPoint of the food location (optional)
  - `servings`: Number of servings (optional)

---

## Contributing

1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add some feature"
   ```
4. Push to the branch:
   ```bash
   git push origin feature/your-feature-name
   ```
5. Open a pull request.

---

## Known Issues

- Location permissions must be granted for the map view to function.
- Some permissions should be manipulated in order for the profile section to work

---

## Future Enhancements

- **Chat Integration**: Allow users to communicate directly through the app.
- **Notification System**: Notify users about new food posts in their vicinity.
- **Rating System**: Enable feedback on food quality and donor reliability.

---

## License

This project is licensed under the MIT License - see the  file for details.

---

## Contact

For any queries or suggestions, feel free to reach out:
- Email: divakaruniashwin789@gmail.com
- LinkedIn: https://www.linkedin.com/in/ashwin-divakaruni-3a5349288/

---

Thank you for contributing to the mission of reducing food wastage with **FoodRescue**!

