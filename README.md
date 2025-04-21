# BudgetTracker

BudgetTracker is a minimalist, user-friendly personal budgeting app for Android that allows users to manually log income and expenses, set monthly budgets, and view visual insights.

## Features

- **User Authentication**: Secure sign-up and sign-in with Firebase Authentication
- **Income & Expense Tracking**: Easily add, edit, and delete transactions
- **Budget Management**: Set monthly budgets for different spending categories
- **Visual Reports**: View spending breakdown and trends with charts
- **Data Export**: Export your financial data to CSV for external analysis
- **Cloud Sync**: Sync your data across devices with Firebase Firestore

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 24 or higher
- Firebase account

### Setup

1. Clone this repository
2. Open the project in Android Studio
3. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
4. Add an Android app to your Firebase project
5. Download the `google-services.json` file and place it in the app directory
6. Enable Firebase Authentication (Email/Password) and Firestore in your Firebase project
7. Build and run the app

## Project Structure

The app follows the MVP (Model-View-Presenter) architecture pattern:

```
BudgetTracker/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/budgettracker/
│   │   │   │   ├── model/                    # Data models
│   │   │   │   ├── presenter/                # Presenter classes
│   │   │   │   │   ├── AuthPresenter.kt
│   │   │   │   │   ├── TransactionPresenter.kt
│   │   │   │   │   ├── BudgetPresenter.kt
│   │   │   │   │   └── ReportPresenter.kt
│   │   │   │   ├── view/                     # UI components
│   │   │   │   │   ├── fragments/            # Fragment implementations
│   │   │   │   │   ├── adapters/             # RecyclerView adapters
│   │   │   │   │   └── AuthActivity.kt       # Auth screens
│   │   │   │   ├── data/                     # Data handling
│   │   │   │   │   ├── local/                # Room Database
│   │   │   │   │   │   ├── entities/         # Database entities
│   │   │   │   │   │   └── dao/              # Data Access Objects
│   │   │   │   │   └── remote/               # Firebase integration
│   │   │   │   ├── util/                     # Helper utilities
│   │   │   │   ├── MainActivity.kt           # Main app container
│   │   │   │   └── App.kt                    # Application class
```

## Libraries Used

- **Firebase Authentication**: For user authentication
- **Firebase Firestore**: For cloud data storage
- **Room**: For local database storage
- **MPAndroidChart**: For visualization of financial data
- **ViewBinding**: For view interactions
- **Coroutines**: For asynchronous operations
- **Glide**: For image loading
- **Material Components**: For modern UI elements

## Usage

1. **Sign Up/Sign In**: Create an account or sign in with your existing credentials
2. **Add Transactions**: Tap the "+" button on the Transactions screen to add income or expenses
3. **Set Budgets**: Set up monthly budgets for different categories on the Budget screen
4. **View Reports**: Check your spending patterns and financial health on the Reports screen
5. **Export Data**: Export your financial data to CSV from the Reports screen

## Project Status

The app is currently functional with all core features implemented:
- User authentication
- Transaction management
- Budget tracking
- Financial reporting
- Data synchronization

## Acknowledgements

- This app was created as a learning project
- Icons are from Google's Material Design icon set