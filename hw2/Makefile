CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -pthread -O2

# example : example.cpp ThreadGroup.h
#	g++ $(CPPFLAGS) $< -o $@

hw2 : hw2.cpp
	g++ $(CPPFLAGS) $< -o $@

clean:
	@rm -r hw2 *.dSYM

all : hw2
	@echo "Made it all!"
