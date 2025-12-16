# Advanced Pattern Based Search Feature

## Overview
The Advanced Pattern Based Search feature (Menu Option 13) provides powerful regex-based searching capabilities for finding students using various pattern types. This feature integrates the `RegexSearchEngine` class with an interactive menu system.

## Features

### 1. Email Domain Pattern Search
- Search by email domain patterns (e.g., `gmail\.com`, `yahoo\..*`)
- Supports full regex patterns
- Highlights matching domain portions
- Returns all students matching the domain pattern

**Example Patterns:**
- `gmail\.com` - Matches Gmail addresses
- `yahoo\..*` - Matches all Yahoo domains
- `.*\.edu$` - Matches educational institutions

### 2. Phone Area Code Pattern Search
- Search by phone area code patterns
- Extracts area codes from phone numbers
- Supports wildcard and regex patterns
- Matches against area code portion only

**Example Patterns:**
- `212` - Matches NYC area code
- `555` - Matches pattern 555
- `[2-5]1[0-5]` - Matches area codes from 210-515

### 3. Student ID Pattern Search
- Search by student ID using wildcard patterns
- Supports regex for complex ID matching
- Case-sensitive matching
- Great for finding student groups/batches

**Example Patterns:**
- `10.*` - All IDs starting with 10
- `1[0-2].*` - IDs starting with 10, 11, or 12
- `.*5$` - All IDs ending with 5

### 4. Name Pattern Search (Regex)
- Full regex pattern matching on student names
- Case-insensitive by default
- Supports complex patterns with alternation and grouping
- Highlights matched portions of names

**Example Patterns:**
- `^A.*` - Names starting with A
- `.*Smith$` - Names ending with Smith
- `^J.*n$` - Names starting with J and ending with n
- `(John|Jane).*` - Names starting with John or Jane

### 5. Custom Regex Pattern Search
- Search any field with custom regex patterns
- Supported fields: Student ID, Name, Email, Phone
- Full flexibility for advanced pattern matching
- Match highlighting with custom patterns

**Example:**
- Field: Email, Pattern: `[a-z]+@[a-z]+\.(com|edu|org)`

## Search Results Display

### Match Highlighting
Results are displayed with match highlighting using:
- `►` - Start of match indicator
- `◄` - End of match indicator
- Format: `► StudentName (ID: #) - matched_value ◄`

### Search Statistics
Each search displays:
- **Pattern**: The regex pattern used
- **Total Scanned**: Number of students searched
- **Matches Found**: Count of matching results
- **Search Time**: Execution time in milliseconds
- **Match Rate**: Percentage of students matching
- **Pattern Complexity**: Simple/Moderate/Complex indicator

## Bulk Operations

After performing a search, you can perform these bulk operations:

### 1. View Full Details
Displays comprehensive information for each matching student:
- Name
- Student ID
- Email
- Phone Number
- GPA
- Status (Regular/Honors)
- Matched Field
- Highlighted Match Value

### 2. Export to CSV
Exports search results in CSV format:
```
StudentID,Name,Email,Phone,GPA,Type,Status
1000,John Smith,john@example.com,(123)456-7890,3.45,Regular,Active
1001,Jane Doe,jane@university.edu,(555)123-4567,3.87,Honors,Active
```

### 3. View Statistics
Displays aggregate statistics for search results:
- Total Matches
- Average GPA of matched students
- Number of Honors Students
- Number of Regular Students

## Technical Implementation

### Classes Involved
- **RegexSearchEngine**: Core search engine with pattern matching
- **Menu**: User interface and navigation
- **Student**: Student data model
- **SearchResult**: Inner class containing:
  - `student`: The Student object
  - `matchedField`: Name of the matched field
  - `matchedValue`: The actual matched value
  - `highlightedMatch`: Value with highlight markers

### Pattern Complexity Hints
- 🟢 **Simple** (0-1 complexity points): Basic literal or wildcard patterns
- 🟡 **Moderate** (2-3 complexity points): Groups, character classes, quantifiers
- 🔴 **Complex** (4+ complexity points): Advanced regex features

### Error Handling
- **Pattern Syntax Errors**: Displays helpful error messages with:
  - Error description
  - Pattern location
  - Common mistake suggestions
  - Examples of correct patterns

## Usage Workflow

```
Main Menu
    ↓
13. Advanced Pattern Based Search
    ↓
Select Search Type (1-5)
    ↓
Enter Pattern
    ↓
Display Results with Highlighting
    ↓
View Statistics
    ↓
Select Bulk Operation (1-4)
    ↓
Display Results / Export / Statistics
    ↓
Back to Pattern Menu or Main Menu
```

## Example Searches

### Example 1: Find all students with Gmail
```
Option: 1 (Email Domain Pattern)
Pattern: gmail\.com
Results: All students with @gmail.com addresses
```

### Example 2: Find students with NYC phone numbers
```
Option: 2 (Phone Area Code)
Pattern: 212
Results: All students with (212) area code
```

### Example 3: Find students in batch 100x
```
Option: 3 (Student ID Pattern)
Pattern: 100.*
Results: All students with IDs starting with 100 (1000, 1001, 1002, etc.)
```

### Example 4: Find names starting with 'A'
```
Option: 4 (Name Pattern)
Pattern: ^A.*
Results: All students whose names start with A
```

### Example 5: Custom pattern for university emails
```
Option: 5 (Custom Pattern)
Field: Email
Pattern: [a-z]+@[a-z]+\.edu$
Results: All students with .edu email addresses
```

## Performance Characteristics
- **Linear Search**: O(n) where n = number of students
- **Pattern Compilation**: Done once, reused for all students
- **Memory**: O(m) where m = number of matches
- **Time Display**: Shows actual search time in milliseconds

## Integration with Existing Features

The Advanced Pattern Search integrates seamlessly with:
- **Real-Time Statistics Dashboard**: Can view aggregate stats
- **Report Generation**: Bulk export supports CSV format
- **Student Management**: Links to student data
- **Scheduled Tasks**: Can be incorporated into automated searches

## Future Enhancement Possibilities

1. **Saved Search Patterns**: Store frequently used patterns
2. **Pattern Library**: Pre-defined patterns for common searches
3. **Advanced Filtering**: Combine multiple patterns with AND/OR
4. **Search History**: Track previous searches
5. **Export Formats**: Support Excel, PDF, JSON
6. **Search Scheduling**: Schedule automated searches
7. **Notifications**: Alert when pattern matches new students
