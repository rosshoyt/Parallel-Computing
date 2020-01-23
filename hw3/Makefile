CLASSES = BitonicSequential.class BitonicPipeline.class BitonicStage.class RandomArrayGenerator.class StageOne.class

all: $(CLASSES)

%.class : %.java
	javac $<

clean:
	@rm -f *.class
