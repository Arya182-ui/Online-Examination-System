import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


class User {
    private String username;
    private String password;
    private String email;
    private String role; // "student", "admin"
    private LocalDateTime lastLogin;

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    public boolean authenticate(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            this.lastLogin = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public void changePassword(String oldPassword, String newPassword) throws Exception {
        if (!this.password.equals(oldPassword)) {
            throw new Exception("Current password is incorrect!");
        }
        this.password = newPassword;
    }
}

class Question {
    private String questionText;
    private String[] options;
    private int correctOption;
    private String difficulty; // "easy", "medium", "hard"
    private String category;
    private String explanation;

    public Question(String questionText, String[] options, int correctOption, 
                   String difficulty, String category, String explanation) {
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
        this.difficulty = difficulty;
        this.category = category;
        this.explanation = explanation;
    }

    // Getters
    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options; }
    public int getCorrectOption() { return correctOption; }
    public String getDifficulty() { return difficulty; }
    public String getCategory() { return category; }
    public String getExplanation() { return explanation; }
}

// Enhanced Result class with detailed analytics
class Result {
    private String username;
    private int score;
    private int total;
    private double percentage;
    private String grade;
    private LocalDateTime examDate;
    private List<Boolean> answerCorrectness;
    private List<Integer> timeSpentPerQuestion;
    private int totalTimeSpent;
    private String examCategory;

    public Result(String username, int score, int total, List<Boolean> answerCorrectness,
                 List<Integer> timeSpentPerQuestion, String examCategory) {
        this.username = username;
        this.score = score;
        this.total = total;
        this.percentage = (total > 0) ? (score * 100.0) / total : 0;
        this.grade = calculateGrade(percentage);
        this.examDate = LocalDateTime.now();
        this.answerCorrectness = answerCorrectness;
        this.timeSpentPerQuestion = timeSpentPerQuestion;
        this.totalTimeSpent = timeSpentPerQuestion.stream().mapToInt(Integer::intValue).sum();
        this.examCategory = examCategory;
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 95) return "A+";
        else if (percentage >= 90) return "A";
        else if (percentage >= 85) return "A-";
        else if (percentage >= 80) return "B+";
        else if (percentage >= 75) return "B";
        else if (percentage >= 70) return "B-";
        else if (percentage >= 65) return "C+";
        else if (percentage >= 60) return "C";
        else if (percentage >= 55) return "D+";
        else if (percentage >= 50) return "D";
        else return "F";
    }

    public void showDetailedResult(List<Question> questions) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("               DETAILED EXAM RESULTS");
        System.out.println("=".repeat(60));
        System.out.println("Student: " + username);
        System.out.println("Exam Category: " + examCategory);
        System.out.println("Date & Time: " + examDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Total Time: " + formatTime(totalTimeSpent));
        System.out.println("-".repeat(60));
        System.out.printf("Score: %d/%d (%.2f%%)\n", score, total, percentage);
        System.out.println("Grade: " + grade);
        System.out.println("-".repeat(60));
        
        // Performance Analysis
        System.out.println("PERFORMANCE ANALYSIS:");
        double avgTimePerQuestion = (double) totalTimeSpent / total;
        System.out.printf("Average time per question: %.1f seconds\n", avgTimePerQuestion);
        
        int correctAnswers = (int) answerCorrectness.stream().mapToLong(b -> b ? 1 : 0).sum();
        System.out.println("Correct answers: " + correctAnswers);
        System.out.println("Incorrect answers: " + (total - correctAnswers));
        
        // Question-wise breakdown
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           QUESTION-WISE BREAKDOWN");
        System.out.println("=".repeat(60));
        
        for (int i = 0; i < answerCorrectness.size(); i++) {
            String status = answerCorrectness.get(i) ? "[CORRECT]" : "[INCORRECT]";
            String timeSpent = formatTime(timeSpentPerQuestion.get(i));
            System.out.printf("Q%d: %s (Time: %s)\n", i + 1, status, timeSpent);
            
            if (!answerCorrectness.get(i) && i < questions.size()) {
                System.out.println("   Explanation: " + questions.get(i).getExplanation());
            }
        }
        System.out.println("=".repeat(60));
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    public void saveDetailedResult() {
        try (FileWriter fw = new FileWriter("detailed_results.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            // Write header if file is empty
            File file = new File("detailed_results.csv");
            if (file.length() == 0) {
                out.println("Username,Category,Date,Score,Total,Percentage,Grade,TotalTime,AvgTimePerQ");
            }
            
            double avgTime = (double) totalTimeSpent / total;
            out.printf("%s,%s,%s,%d,%d,%.2f,%s,%s,%.1f\n",
                username, examCategory, 
                examDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                score, total, percentage, grade, formatTime(totalTimeSpent), avgTime);
                
        } catch (IOException e) {
            System.out.println("Error saving detailed result: " + e.getMessage());
        }
    }

    // Getters
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
}

// Enhanced Exam class with multiple exam modes
class Exam {
    private List<Question> questions;
    private int score;
    private List<Boolean> answerCorrectness;
    private List<Integer> timeSpentPerQuestion;
    private Scanner scanner;
    private int timePerQuestion;
    private String examMode; // "practice", "timed", "adaptive"
    private String category;

    public Exam(List<Question> questions, int timePerQuestion, String examMode, String category) {
        this.questions = questions;
        this.score = 0;
        this.answerCorrectness = new ArrayList<>();
        this.timeSpentPerQuestion = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.timePerQuestion = timePerQuestion;
        this.examMode = examMode;
        this.category = category;
    }

    public Result startExam(String username) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("               EXAM STARTED");
        System.out.println("=".repeat(60));
        System.out.println("Mode: " + examMode.toUpperCase());
        System.out.println("Category: " + category);
        System.out.println("Questions: " + questions.size());
        System.out.println("Time per question: " + timePerQuestion + " seconds");
        System.out.println("=".repeat(60));

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            displayQuestion(q, i + 1);
            
            long startTime = System.currentTimeMillis();
            int userAnswer = getUserAnswerWithTimer(timePerQuestion);
            long endTime = System.currentTimeMillis();
            
            int timeSpent = (int) ((endTime - startTime) / 1000);
            timeSpentPerQuestion.add(Math.min(timeSpent, timePerQuestion));
            
            evaluateAnswer(q, userAnswer, i + 1);
            
            // Show progress
            displayProgress(i + 1, questions.size());
        }

        return new Result(username, score, questions.size(), answerCorrectness, 
                         timeSpentPerQuestion, category);
    }

    private void displayQuestion(Question q, int questionNum) {
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("Question %d of %d [%s]\n", questionNum, questions.size(), 
                         q.getDifficulty().toUpperCase());
        System.out.println("=".repeat(60));
        System.out.println(q.getQuestionText());
        System.out.println();
        
        String[] options = q.getOptions();
        for (int j = 0; j < options.length; j++) {
            System.out.printf("%d. %s\n", j + 1, options[j]);
        }
    }

    private void evaluateAnswer(Question q, int userAnswer, int questionNum) {
        System.out.println("\n" + "-".repeat(50));
        
        if (userAnswer == q.getCorrectOption()) {
            System.out.println("[CORRECT] Well done!");
            score++;
            answerCorrectness.add(true);
        } else if (userAnswer == -1) {
            System.out.println("[TIMEOUT] No answer recorded - Time expired!");
            System.out.println("Correct answer: " + q.getCorrectOption() + 
                             ". " + q.getOptions()[q.getCorrectOption() - 1]);
            answerCorrectness.add(false);
        } else {
            System.out.println("[INCORRECT]");
            System.out.println("Your answer: " + userAnswer + 
                             ". " + q.getOptions()[userAnswer - 1]);
            System.out.println("Correct answer: " + q.getCorrectOption() + 
                             ". " + q.getOptions()[q.getCorrectOption() - 1]);
            answerCorrectness.add(false);
        }
        
        // Show explanation for wrong answers or if in practice mode
        if ((userAnswer != q.getCorrectOption() || examMode.equals("practice")) 
            && !q.getExplanation().isEmpty()) {
            System.out.println("Explanation: " + q.getExplanation());
        }
        
        System.out.printf("Current Score: %d/%d (%.1f%%)\n", 
                         score, questionNum, (score * 100.0) / questionNum);
        System.out.println("-".repeat(50));
    }

    private void displayProgress(int current, int total) {
        int progressWidth = 30;
        int progress = (current * progressWidth) / total;
        
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("[");
        for (int i = 0; i < progressWidth; i++) {
            if (i < progress) progressBar.append("=");
            else progressBar.append("-");
        }
        progressBar.append("]");
        
        System.out.printf("\nProgress: %s %d/%d (%.1f%%)\n", 
                         progressBar.toString(), current, total, (current * 100.0) / total);
    }

    private int getUserAnswerWithTimer(int seconds) {
        if (examMode.equals("practice")) {
            return getUserAnswerNormal(); // No timer in practice mode
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TIME LIMIT: " + seconds + " SECONDS");
        System.out.println("=".repeat(50));
        System.out.print("Your answer (1-4): ");
        
        final boolean[] answered = {false};
        final int[] userAnswer = {-1};
        final boolean[] timedOut = {false};
        
        // Enhanced timer with visual countdown
        Thread timerThread = new Thread(() -> {
            try {
                for (int i = seconds; i > 0; i--) {
                    if (answered[0]) break;
                    
                    int progress = (i * 20) / seconds;
                    StringBuilder bar = new StringBuilder();
                    for (int j = 0; j < 20; j++) {
                        if (j < progress) {
                            if (i <= 5) bar.append("#"); // Different char when time is running out
                            else if (i <= 10) bar.append("*"); // Warning
                            else bar.append("="); // Normal
                        } else {
                            bar.append("-");
                        }
                    }
                    
                    String urgency = i <= 5 ? " *** HURRY UP! ***" : 
                                   i <= 10 ? " ** Time running out! **" : "";
                    System.out.print("\r[TIMER] " + bar + " " + String.format("%2d", i) + "s" + urgency + "  ");
                    System.out.flush();
                    
                    Thread.sleep(1000);
                }
                
                if (!answered[0]) {
                    timedOut[0] = true;
                    System.out.print("\r*** TIME'S UP! ***                                        ");
                    System.out.println();
                }
            } catch (InterruptedException e) {
                // Timer interrupted
            }
        });
        
        Thread inputThread = new Thread(() -> {
            try {
                String input = scanner.nextLine();
                if (!timedOut[0]) {
                    answered[0] = true;
                    System.out.print("\r                                                           \r");
                    try {
                        int answer = Integer.parseInt(input.trim());
                        if (answer >= 1 && answer <= 4) {
                            userAnswer[0] = answer;
                            System.out.println("[ANSWER RECORDED] " + answer);
                        } else {
                            System.out.println("[ERROR] Invalid option! Must be 1-4.");
                            userAnswer[0] = -1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid input! Must be a number.");
                        userAnswer[0] = -1;
                    }
                }
            } catch (Exception e) {
                userAnswer[0] = -1;
            }
        });
        
        timerThread.start();
        inputThread.start();
        
        try {
            inputThread.join(seconds * 1000L + 1000);
            timerThread.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (timerThread.isAlive()) timerThread.interrupt();
        if (inputThread.isAlive()) inputThread.interrupt();
        
        return timedOut[0] || userAnswer[0] == -1 ? -1 : userAnswer[0];
    }

    private int getUserAnswerNormal() {
        System.out.print("\nYour answer (1-4): ");
        try {
            String input = scanner.nextLine();
            int answer = Integer.parseInt(input.trim());
            if (answer >= 1 && answer <= 4) {
                return answer;
            } else {
                System.out.println("[ERROR] Invalid option! Must be 1-4.");
                return -1;
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input! Must be a number.");
            return -1;
        }
    }
}

// Enhanced Statistics class for tracking performance
class Statistics {
    public static void showUserStatistics(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("detailed_results.csv"));
            if (lines.size() <= 1) {
                System.out.println("No exam history found for " + username);
                return;
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("           PERFORMANCE STATISTICS - " + username.toUpperCase());
            System.out.println("=".repeat(60));

            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts[0].equals(username)) {
                    // Parse and display statistics
                    System.out.printf("Exam: %s | Score: %s/%s (%.2f%%) | Grade: %s | Date: %s\n",
                        parts[1], parts[3], parts[4], Double.parseDouble(parts[5]), parts[6], parts[2]);
                }
            }
            System.out.println("=".repeat(60));
        } catch (IOException e) {
            System.out.println("Error reading statistics: " + e.getMessage());
        }
    }
}

// Main Enhanced Exam System
public class EnhancedExamSystem {
    private static List<User> users = new ArrayList<>();
    private static Map<String, List<Question>> questionBank = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    // Safe input reading method to prevent NoSuchElementException
    private static String safeNextLine(String prompt) {
        System.out.print(prompt);
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            } else {
                System.out.println("\n[ERROR] Input stream closed unexpectedly.");
                System.exit(1);
                return "";
            }
        } catch (Exception e) {
            System.out.println("\n[ERROR] Input error: " + e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   ENHANCED ONLINE EXAMINATION SYSTEM");
        System.out.println("=".repeat(60));
        
        initializeUsers();
        initializeQuestionBank();
        
        User currentUser = login();
        if (currentUser != null) {
            if (currentUser.getRole().equals("admin")) {
                adminPanel(currentUser);
            } else {
                studentPanel(currentUser);
            }
        }
        
        System.out.println("\nThank you for using the Enhanced Examination System!");
    }

    private static void initializeUsers() {
        users.add(new User("arya", "arya@119", "arya@email.com", "student"));
        users.add(new User("user", "user123", "user@email.com", "student"));
        users.add(new User("admin", "admin123", "admin@email.com", "admin"));
        users.add(new User("john", "john123", "john@email.com", "student"));
        users.add(new User("alice", "alice123", "alice@email.com", "student"));
    }

    private static void initializeQuestionBank() {
        // Java Programming Questions
        List<Question> javaQuestions = new ArrayList<>();
        javaQuestions.add(new Question(
            "Which keyword is used to create a class in Java?",
            new String[]{"class", "Class", "new", "create"},
            1, "easy", "Java Programming",
            "The 'class' keyword is used to define a class in Java."
        ));
        javaQuestions.add(new Question(
            "What is the default value of a boolean variable in Java?",
            new String[]{"true", "false", "0", "null"},
            2, "easy", "Java Programming",
            "Boolean variables are initialized to false by default in Java."
        ));
        javaQuestions.add(new Question(
            "Which method is called when an object is created?",
            new String[]{"main()", "start()", "constructor", "init()"},
            3, "medium", "Java Programming",
            "Constructor is a special method called when an object is instantiated."
        ));
        javaQuestions.add(new Question(
            "What does JVM stand for?",
            new String[]{"Java Virtual Machine", "Java Version Manager", "Java Variable Method", "Java Visual Model"},
            1, "easy", "Java Programming",
            "JVM stands for Java Virtual Machine - it executes Java bytecode."
        ));

        // Data Structures Questions
        List<Question> dsQuestions = new ArrayList<>();
        dsQuestions.add(new Question(
            "What is the time complexity of searching in a balanced BST?",
            new String[]{"O(1)", "O(log n)", "O(n)", "O(n²)"},
            2, "medium", "Data Structures",
            "Balanced BST maintains O(log n) height, making search operations logarithmic."
        ));
        dsQuestions.add(new Question(
            "Which data structure follows LIFO principle?",
            new String[]{"Queue", "Stack", "Array", "LinkedList"},
            2, "easy", "Data Structures",
            "Stack follows Last In First Out (LIFO) principle."
        ));
        dsQuestions.add(new Question(
            "What is the worst-case time complexity of QuickSort?",
            new String[]{"O(n log n)", "O(n²)", "O(n)", "O(log n)"},
            2, "hard", "Data Structures",
            "QuickSort has O(n²) worst-case when pivot is always smallest or largest element."
        ));

        // General Knowledge Questions
        List<Question> gkQuestions = new ArrayList<>();
        gkQuestions.add(new Question(
            "What is the capital of India?",
            new String[]{"Mumbai", "New Delhi", "Kolkata", "Chennai"},
            2, "easy", "General Knowledge",
            "New Delhi is the capital city of India."
        ));
        gkQuestions.add(new Question(
            "Who developed the theory of relativity?",
            new String[]{"Newton", "Einstein", "Galileo", "Tesla"},
            2, "medium", "General Knowledge",
            "Albert Einstein developed both special and general theories of relativity."
        ));
        gkQuestions.add(new Question(
            "What does CPU stand for?",
            new String[]{"Computer Processing Unit", "Central Processing Unit", "Core Processing Unit", "Central Program Unit"},
            2, "easy", "General Knowledge",
            "CPU stands for Central Processing Unit - the main component that executes instructions."
        ));

        questionBank.put("Java Programming", javaQuestions);
        questionBank.put("Data Structures", dsQuestions);
        questionBank.put("General Knowledge", gkQuestions);
    }

    private static User login() {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("\nEnter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            for (User user : users) {
                if (user.authenticate(username, password)) {
                    System.out.println("\n[SUCCESS] Login successful! Welcome, " + username + "!");
                    System.out.println("Role: " + user.getRole().toUpperCase());
                    return user;
                }
            }
            attempts++;
            System.out.println("[ERROR] Invalid credentials! Attempts remaining: " + (3 - attempts));
        }
        System.out.println("[LOCKED] Too many failed attempts. System locked!");
        return null;
    }

    private static void studentPanel(User user) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           STUDENT DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. Take Practice Exam (No Timer)");
            System.out.println("2. Take Timed Exam");
            System.out.println("3. View My Statistics");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            System.out.print("\nChoose an option: ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("[ERROR] Please enter a valid option!");
                    continue;
                }
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        takeExam(user, "practice");
                        break;
                    case 2:
                        takeExam(user, "timed");
                        break;
                    case 3:
                        Statistics.showUserStatistics(user.getUsername());
                        break;
                    case 4:
                        changePassword(user);
                        break;
                    case 5:
                        System.out.println("Goodbye, " + user.getUsername() + "!");
                        return;
                    default:
                        System.out.println("[ERROR] Invalid option!");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number!");
            }
        }
    }

    private static void adminPanel(User admin) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           ADMIN DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. View All Users");
            System.out.println("2. View All Statistics");
            System.out.println("3. Add New User");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            System.out.print("\nChoose an option: ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("[ERROR] Please enter a valid option!");
                    continue;
                }
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        viewAllUsers();
                        break;
                    case 2:
                        viewAllStatistics();
                        break;
                    case 3:
                        addNewUser();
                        break;
                    case 4:
                        changePassword(admin);
                        break;
                    case 5:
                        System.out.println("Goodbye, " + admin.getUsername() + "!");
                        return;
                    default:
                        System.out.println("[ERROR] Invalid option!");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number!");
            }
        }
    }

    private static void takeExam(User user, String mode) {
        System.out.println("\nAvailable Categories:");
        String[] categories = questionBank.keySet().toArray(new String[0]);
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i] + 
                             " (" + questionBank.get(categories[i]).size() + " questions)");
        }

        System.out.print("\nSelect category: ");
        try {
            int categoryChoice = Integer.parseInt(scanner.nextLine());
            if (categoryChoice < 1 || categoryChoice > categories.length) {
                System.out.println("[ERROR] Invalid category!");
                return;
            }

            String selectedCategory = categories[categoryChoice - 1];
            List<Question> questions = questionBank.get(selectedCategory);
            
            int timePerQuestion = mode.equals("practice") ? 60 : 30;
            Exam exam = new Exam(questions, timePerQuestion, mode, selectedCategory);
            Result result = exam.startExam(user.getUsername());
            
            result.showDetailedResult(questions);
            result.saveDetailedResult();
            
            // Provide feedback based on performance
            provideFeedback(result);
            
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid number!");
        }
    }

    private static void provideFeedback(Result result) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           PERFORMANCE FEEDBACK");
        System.out.println("=".repeat(50));
        
        double percentage = result.getPercentage();
        if (percentage >= 90) {
            System.out.println("[EXCELLENT] Outstanding performance!");
            System.out.println("   You have mastered this topic!");
        } else if (percentage >= 75) {
            System.out.println("[GREAT JOB] Very good performance!");
            System.out.println("   Keep up the excellent work!");
        } else if (percentage >= 60) {
            System.out.println("[GOOD EFFORT] You're on the right track!");
            System.out.println("   A bit more practice will make you perfect!");
        } else {
            System.out.println("[KEEP TRYING] Don't give up!");
            System.out.println("   Review the topics and practice more!");
        }
        System.out.println("=".repeat(50));
    }

    private static void changePassword(User user) {
        System.out.print("\nEnter current password: ");
        String currentPassword = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("[ERROR] Passwords don't match!");
            return;
        }

        try {
            user.changePassword(currentPassword, newPassword);
            System.out.println("[SUCCESS] Password changed successfully!");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void viewAllUsers() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    ALL USERS");
        System.out.println("=".repeat(60));
        System.out.printf("%-15s %-20s %-10s %-20s\n", "Username", "Email", "Role", "Last Login");
        System.out.println("-".repeat(60));
        
        for (User user : users) {
            String lastLogin = user.getLastLogin() != null ? 
                user.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never";
            System.out.printf("%-15s %-20s %-10s %-20s\n", 
                user.getUsername(), user.getEmail(), user.getRole(), lastLogin);
        }
        System.out.println("=".repeat(60));
    }

    private static void viewAllStatistics() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("detailed_results.csv"));
            if (lines.size() <= 1) {
                System.out.println("No exam records found.");
                return;
            }

            System.out.println("\n" + "=".repeat(80));
            System.out.println("                       ALL EXAM STATISTICS");
            System.out.println("=".repeat(80));
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                System.out.printf("%-12s | %-18s | %s/%s (%.1f%%) | Grade: %s | %s\n",
                    parts[0], parts[1], parts[3], parts[4], 
                    Double.parseDouble(parts[5]), parts[6], parts[2]);
            }
            System.out.println("=".repeat(80));
        } catch (IOException e) {
            System.out.println("[ERROR] Error reading statistics: " + e.getMessage());
        }
    }

    private static void addNewUser() {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine();
        
        // Check if user already exists
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("[ERROR] Username already exists!");
                return;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter role (student/admin): ");
        String role = scanner.nextLine();

        if (!role.equals("student") && !role.equals("admin")) {
            System.out.println("[ERROR] Invalid role! Must be 'student' or 'admin'");
            return;
        }

        users.add(new User(username, password, email, role));
        System.out.println("[SUCCESS] User added successfully!");
    }
}