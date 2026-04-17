# 🛠️ MacroJava → MiniJava Compiler (6-Stage Pipeline)

A full-stack **multi-stage compiler** for a subset of Java, built as part of a systems/compiler design project. The compiler transforms **MacroJava → MiniJava → Intermediate Representations → MIPS Assembly**, closely mirroring the architecture of real-world optimizing compilers.

---

## 🚀 Overview

This project implements a **6-stage compilation pipeline**, covering the complete journey from high-level source code to low-level executable instructions:

MacroJava  
   ↓ (Macro Expansion + Parsing)  
MiniJava  
   ↓ (Semantic Analysis)  
Type-Checked MiniJava  
   ↓ (IR Generation)  
MiniIR  
   ↓ (Simplification)  
MicroIR  
   ↓ (Register Allocation)  
MiniRA  
   ↓ (Code Generation)  
MIPS Assembly (.s)  

Each stage is modular, composable, and validated independently.

---

## ⚙️ Tech Stack

- **C / Flex / Bison** → Lexical analysis & parsing  
- **Java / JTB / JavaCC** → AST generation & visitor-based transformations  
- **MIPS Assembly** → Final code generation  
- **Custom IRs** → MiniIR, MicroIR, MiniRA  

---

## 🧩 Compiler Stages

### 1. Macro Expansion & Parsing
- Built using **Flex + Bison**
- Converts **MacroJava → MiniJava**
- Handles macro expansion and syntax validation  
- Outputs valid MiniJava or fails gracefully

---

### 2. Semantic Analysis (Type Checking)
- Built using **JTB + JavaCC (Visitor Pattern)**
- Performs:
  - Type checking  
  - Symbol table construction  
  - Scope resolution  
- Detects:
  - Type errors  
  - Undeclared identifiers  

---

### 3. Intermediate Code Generation (MiniIR)
- Translates MiniJava AST → **MiniIR**
- Introduces explicit control flow and temporaries  
- Ensures semantic equivalence with source

---

### 4. IR Simplification (MicroIR)
- Converts MiniIR → **MicroIR**
- Simplifies instruction structure  
- Prepares code for register allocation  

---

### 5. Register Allocation (MiniRA)
- Maps temporaries → registers  
- Handles limited register constraints  
- Produces **MiniRA representation** suitable for backend

---

### 6. Code Generation (MIPS)
- Generates **MIPS assembly (.s)**  
- Preserves correctness across transformations  
- Compatible with standard MIPS interpreters  

---

## ▶️ How to Run

### Stage 1: MacroJava → MiniJava

    bison -d P1.y
    flex P1.l
    gcc P1.tab.c lex.yy.c -lfl -o P1
    ./P1 < input.java > output.java

---

### Stage 2: Type Checking

    java P2 < input.java

Output:
- Program type checked successfully  
- Type error  
- Symbol not found  

---

### Stage 3: MiniJava → MiniIR

    java P3 < input.java > output.miniIR

---

### Stage 4: MiniIR → MicroIR

    java P4 < input.miniIR > output.microIR

---

### Stage 5: MicroIR → MiniRA

    java P5 < input.microIR > output.RA

---

### Stage 6: MiniRA → MIPS

    java P6 < input.RA > output.s

---

## ✅ Validation & Testing

- Parsing validation via grammar conformance  
- Semantic correctness via type checker  
- IR correctness via interpreters (pgi.jar, kgi.jar)  
- End-to-end validation by comparing outputs across stages  

---

## 🧠 Key Concepts Implemented

- Lexical Analysis & Parsing (LL/LR parsing)  
- Abstract Syntax Trees (ASTs)  
- Visitor Pattern for compiler passes  
- Symbol Tables & Scope Resolution  
- Type Systems  
- Intermediate Representations (IR design)  
- Register Allocation  
- Code Generation  

---

## 📌 Design Philosophy

- Modular pipeline → Each stage is independently testable  
- Semantic preservation → Output correctness verified at every step  
- Separation of concerns → Frontend, middle-end, backend clearly divided  
- Compiler realism → Mirrors structure of production compilers  

---

## 📈 Future Improvements

- Liveness analysis + graph-coloring register allocation  
- Optimization passes (dead code elimination, constant folding, CSE)  
- SSA-based IR  
- Better error diagnostics  
- Support for larger Java subset  

---

## 👨‍💻 Author

Deepanjan Das  
