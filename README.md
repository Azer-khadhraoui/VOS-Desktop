# VOS-Desktop

A modern JavaFX-based desktop application for recruitment and contract management. This application provides a user-friendly interface for managing recruitment processes and employment contracts with a sleek, contemporary design.

## 📋 Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Project Structure Details](#project-structure-details)

## ✨ Features

- **Recruitment Management**: Create, view, and manage recruitment records
- **Contract Management**: Track and manage employment contracts
- **Modern UI**: Sleek, responsive JavaFX interface with theme toggle support
- **Splash Screen**: Professional splash screen on application startup
- **Theme Support**: Dark/light theme toggle for improved user experience
- **Database Integration**: Persistent data storage with database backend

## 🛠️ Technologies

- **Language**: Java
- **UI Framework**: JavaFX
- **Build Tool**: Maven
- **Database**: MySQL/Database (via custom DB utility)
- **Styling**: CSS (Modern themes)
- **Architecture Pattern**: MVC (Model-View-Controller)

## 📁 Project Structure

```
VOS-Desktop/
├── src/main/java/
│   ├── application/          # Main application entry points
│   │   ├── MainApp.java
│   │   ├── SplashScreenLauncher.java
│   │   └── ThemeToggle.java
│   ├── controllers/          # JavaFX Controllers
│   │   ├── MainController.java
│   │   ├── RecrutementController.java
│   │   ├── ContratController.java
│   │   ├── AdministrationController.java
│   │   └── SplashScreenController.java
│   ├── entities/            # Data Models
│   │   ├── Recrutement.java
│   │   ├── Contrat.java
│   │   └── RecrutementGroup.java
│   ├── services/            # Business Logic
│   │   ├── ServiceRecrutement.java
│   │   └── ServiceContrat.java
│   ├── utils/               # Utility Classes
│   │   └── MyDB.java
│   └── resources/
│       ├── views/           # FXML files
│       ├── styles/          # CSS stylesheets
│       └── Images/          # Application images
└── pom.xml
```

## 📋 Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6+
- MySQL Database (or compatible database)
- JavaFX SDK (typically included via Maven)

## 🚀 Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd VOS-Desktop
   ```

2. **Configure database connection**:
   - Update the database credentials in `src/main/java/utils/MyDB.java`
   - Ensure your MySQL server is running

3. **Build the project**:
   ```bash
   mvn clean install
   ```

## ▶️ Running the Application

### Option 1: Run from Maven
```bash
mvn javafx:run
```

### Option 2: Run the JAR file
```bash
java -jar target/VOS-Desktop.jar
```

### Option 3: Run from IDE
- Open the project in your IDE (IntelliJ IDEA, Eclipse, etc.)
- Run `MainApp.java` as a Java Application

## 🏗️ Project Structure Details

### Application Layer (`application/`)
- **MainApp.java**: Entry point for the application
- **SplashScreenLauncher.java**: Manages splash screen display
- **ThemeToggle.java**: Handles application theme switching

### Controller Layer (`controllers/`)
- **MainController.java**: Main window controller
- **RecrutementController.java**: Manages recruitment UI and interactions
- **ContratController.java**: Manages contract UI and interactions
- **AdministrationController.java**: Administrative functionality
- **SplashScreenController.java**: Splash screen logic

### Entity Layer (`entities/`)
- **Recrutement.java**: Recruitment data model
- **Contrat.java**: Contract data model
- **RecrutementGroup.java**: Grouping for recruitment records
- **RecrutementTableRow.java**: Table row representation

### Service Layer (`services/`)
- **ServiceRecrutement.java**: Business logic for recruitment operations
- **ServiceContrat.java**: Business logic for contract operations

### Utility Layer (`utils/`)
- **MyDB.java**: Database connection and operations manager

## 📝 License

[Manai Fares]

## 💬 Support

For support, please contact the development team or open an issue on the repository.

---

**Last Updated**: February 2026