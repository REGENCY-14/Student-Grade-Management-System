# Student Grade Management System - Project Structure

## Overview
The project has been reorganized with a clean, layered architecture following best practices for Java project organization.

## Directory Structure

```
src/main/java/
├── core/                          # Core domain classes
│   ├── Student.java              # Abstract base Student class
│   ├── Grade.java                # Grade domain model
│   ├── Subject.java              # Subject domain model
│   ├── CoreSubject.java          # Core subject implementation
│   └── ElectiveSubject.java      # Elective subject implementation
│
├── models/                        # Student model variants and services
│   ├── HonorsStudent.java        # Honors student type
│   ├── RegularStudent.java       # Regular student type
│   ├── StudentFactory.java       # Student creation factory
│   └── StudentService.java       # Student business logic service
│
├── manager/                       # Business logic managers
│   ├── GradeManager.java         # Grade management operations
│   ├── IGradeManager.java        # Grade manager interface
│   ├── CacheManager.java         # Caching layer
│   └── FileFormatManager.java    # File format handling (CSV, JSON, Binary)
│
├── ui/                            # User interface layer
│   ├── Main.java                 # Application entry point
│   └── Menu.java                 # Console menu system
│
├── analytics/                     # Analytics and reporting
│   ├── StatisticsDashboard.java  # Dashboard display logic
│   └── StatsCalculator.java      # Statistical calculations
│
├── scheduler/                     # Task scheduling
│   ├── TaskScheduler.java        # Main scheduler implementation
│   └── ScheduledTask.java        # Task definition and types
│
├── search/                        # Search and reporting functionality
│   ├── RegexSearchEngine.java    # Pattern-based search
│   └── ConcurrentReportGenerator.java  # Parallel report generation
│
├── imports/                       # Data import utilities
│   ├── GenerateSampleBinaryData.java   # Sample data generation
│   └── binary/                   # Binary format files
│       └── sample_grades_import.bin
│
├── exception/                     # Custom exceptions
│   ├── FileImportException.java
│   ├── InvalidGradeException.java
│   ├── InvalidStudentDataException.java
│   ├── StudentNotFoundException.java
│   ├── SubjectNotFoundException.java
│   ├── GradeStorageFullException.java
│   └── InvalidReportFormatException.java
│
├── validators/                    # Input validation
│   ├── IValidator.java           # Validator interface
│   ├── StudentDataValidator.java # Student data validation
│   ├── AgeValidator.java         # Age validation
│   ├── NameValidator.java        # Name validation
│   ├── EmailValidator.java       # Email validation
│   └── PhoneValidator.java       # Phone validation
│
├── calculations/                  # Grade calculations
│   ├── GradeCalculator.java      # Calculation logic
│   └── IGradeCalculator.java     # Calculator interface
│
├── interfaces/                    # Key domain interfaces
│   ├── IStudentAcademicPerformance.java
│   ├── IStudentIdentity.java
│   └── IStudentType.java
│
├── services/                      # Service layer interfaces
│   ├── IGradeRepository.java.bak
│   ├── IStudentRepository.java.bak
│   └── ServiceLocator.java.bak
│
├── reporting/                     # Report formatting
│   ├── IOutputFormatter.java
│   ├── IReportGenerator.java
│   └── ConsoleOutputFormatter.java
│
├── generators/                    # Utility generators
│   ├── IIdGenerator.java         # ID generation interface
│   └── SequentialIdGenerator.java # Sequential ID implementation
│
└── examples/                      # SOLID principle examples
    └── SOLIDExample.java         # Design pattern demonstrations
```

## Package Organization by Layer

### Domain Layer (core)
- **Purpose**: Core business domain classes
- **Contains**: Student, Grade, Subject and their implementations
- **Responsibility**: Define the business entity models

### Model Layer (models)
- **Purpose**: Different student types and student-related services
- **Contains**: Student type implementations (HonorsStudent, RegularStudent) and factory
- **Responsibility**: Encapsulate student variants and creation logic

### Manager Layer (manager)
- **Purpose**: Business logic and data management
- **Contains**: Grade management, caching, and file format handling
- **Responsibility**: Orchestrate domain operations

### Analytics Layer (analytics)
- **Purpose**: Statistics and reporting functionality
- **Contains**: Dashboard and statistical calculations
- **Responsibility**: Process and display analytical data

### Scheduler Layer (scheduler)
- **Purpose**: Asynchronous task execution
- **Contains**: Task scheduler and task definitions
- **Responsibility**: Schedule and execute background tasks

### Search Layer (search)
- **Purpose**: Search and concurrent report generation
- **Contains**: Regex search engine and concurrent report generation
- **Responsibility**: Provide search capabilities and generate reports

### UI Layer (ui)
- **Purpose**: User-facing interfaces
- **Contains**: Menu system and application entry point
- **Responsibility**: Handle user interaction

### Cross-Cutting Concerns

- **exception/**: Custom exceptions for the entire application
- **validators/**: Input validation utilities
- **calculations/**: Grade calculation algorithms
- **services/**: Repository and service locator patterns
- **reporting/**: Output formatting interfaces
- **interfaces/**: Key domain interfaces
- **generators/**: ID and other utility generators
- **imports/**: Data import utilities
- **examples/**: Reference implementations and examples

## Key Architectural Principles

### Separation of Concerns
Each package has a single, well-defined responsibility, making the codebase easier to maintain and extend.

### Dependency Management
- Higher-level packages (ui, analytics) depend on lower-level packages (core, manager)
- Cross-cutting concerns (validators, exception) are accessible to all packages
- No circular dependencies

### Layered Architecture
```
┌─────────────────────┐
│   UI (ui, examples) │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ Presentation Layer  │ (analytics, scheduler, search)
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ Business Logic      │ (manager, models)
│ (manager, models)   │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ Domain Layer        │ (core)
└─────────────────────┘
           │
┌──────────▼──────────────────────────┐
│ Cross-Cutting Concerns              │
│ (exception, validators, imports,    │
│  calculations, generators, services)│
└─────────────────────────────────────┘
```

## Benefits of This Structure

1. **Maintainability**: Related functionality is grouped together
2. **Scalability**: Easy to add new features in appropriate packages
3. **Testability**: Clear boundaries make unit testing straightforward
4. **Reusability**: Well-organized packages facilitate code reuse
5. **Collaboration**: Clear structure helps team members understand the codebase
6. **IDE Navigation**: Better code organization improves IDE support

## How to Use This Structure

### Adding a New Feature
1. Identify which layer the feature belongs to
2. Create the class in the appropriate package
3. Ensure package declarations match the directory structure
4. Update imports as needed throughout the codebase

### Finding Code
- **Domain Logic**: Look in `core/` and `models/`
- **Business Operations**: Check `manager/`
- **User Interaction**: See `ui/`
- **Reporting/Analytics**: Check `analytics/` and `search/`
- **Validation**: Look in `validators/`
- **Data Import**: Check `imports/`

### Dependencies
- The UI layer should only depend on public interfaces from lower layers
- Business logic shouldn't depend on UI components
- Domain classes remain independent of implementation details
