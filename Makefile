# Папка с изходните файлове
SRC_DIR := src
# Папка за компилирани файлове
OUT_DIR := out
# Папка с библиотеки
LIB_DIR := lib

# Всички .java файлове
JAVA_FILES := $(shell find $(SRC_DIR) -name "*.java")

# Компилатор и опции
JAVAC := javac
JFLAGS := -d $(OUT_DIR) -cp "$(LIB_DIR)/*"

# Главен клас за стартиране
MAIN_CLASS := CLI

# Default target
all: compile run

# Създава директорията out, ако не съществува
$(OUT_DIR):
	mkdir -p $(OUT_DIR)

# Компилиране на всички .java файлове
compile: $(OUT_DIR)
	$(JAVAC) $(JFLAGS) $(JAVA_FILES)

# Стартиране на програмата
run: compile
	java -cp "$(OUT_DIR):$(LIB_DIR)/*" $(MAIN_CLASS)

# Почиства .class файловете
clean:
	rm -rf $(OUT_DIR)/*

.PHONY: all compile run clean
