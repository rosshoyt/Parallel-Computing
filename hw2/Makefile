CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -pthread

# example : example.cpp ThreadGroup.h
#	g++ $(CPPFLAGS) $< -o $@

hw2 : hw2.cpp
	g++ $(CPPFLAGS) $< -o $@

hw2-optimized : hw2.cpp
	g++ $(CPPFLAGS) $< -O2 -o $@

clean:
	@rm -r hw2 hw2-optimized *.dSYM

all : hw2 hw2-optimized
	@echo "Made it all!"
