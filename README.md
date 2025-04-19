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

- **Model**: Data classes and repository in `data` package
- **View**: UI components in `view` package
- **Presenter**: Business logic in `presenter` package

## Libraries Used

- **Firebase Authentication**: For user authentication
- **Firebase Firestore**: For cloud data storage
- **Room**: For local database storage
- **MPAndroidChart**: For visualization of financial data
- **Jetpack Compose**: For modern UI components
- **Android Architecture Components**: For lifecycle-aware components

## Usage

1. **Sign Up/Sign In**: Create an account or sign in with your existing credentials
2. **Add Transactions**: Tap the "+" button on the Transactions screen to add income or expenses
3. **Set Budgets**: Set up monthly budgets for different categories on the Budget screen
4. **View Reports**: Check your spending patterns and financial health on the Reports screen
5. **Export Data**: Export your financial data to CSV from the Reports screen

## Acknowledgements

- This app was created as a learning project.
- Icons are from Google's Material Design icon set

Retrieved transaction: Transaction(id=19, amount=200.0, description=Fun, date=Fri Apr 18 16:17:33 PDT 2025, categoryId=4, isIncome=false, userId=ZpHyEgoEz7PABM7o4f2AeXJytyx2)
Retrieved transaction: Transaction(id=18, amount=3400.0, description=Payme, date=Fri Apr 18 14:36:18 PDT 2025, categoryId=8, isIncome=true, userId=ZpHyEgoEz7PABM7o4f2AeXJytyx2)

ReportPresenter: Calculating spending for 0 expense transactions out of 0 total
ReportPresenter: No real spending data, creating sample data from categories