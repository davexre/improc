.DEFAULT_GOAL = all
.PHONY: clean all

CXX      = avr-g++
CXXFLAGS = \
 -I"../arduino" \
 -I"../Library" \
 -I"../Slavian" \
 -Wall -Os -fpack-struct -fshort-enums -ffunction-sections -fdata-sections \
 -std="c++11" -funsigned-char -funsigned-bitfields -fno-exceptions -mmcu=atmega328p -DF_CPU=16000000UL
LDFLAGS  =

SOURCE_DIR := ./
OUTPUT_DIR := ./output/

rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))
SOURCE_FILES  := $(call rwildcard, $(SOURCE_DIR), *.cpp *.c)
OBJS          := $(addprefix $(OUTPUT_DIR), $(addsuffix .o, $(basename $(SOURCE_FILES))))
DEPENDANCIES  := $(addprefix $(OUTPUT_DIR), $(addsuffix .d, $(basename $(SOURCE_FILES))))
#DEPENDANCIES := $(addprefix $(OUTPUT_DIR), $(SOURCE_FILES:.cpp=.d))
LIBS          := $(OUTPUT_DIR)libArduino.a

force:

$(OUTPUT_DIR)%.d:
	@mkdir -p $(dir $@)
	$(CXX) -M -MT "$@ $(patsubst %.d,%.o,$@)" $(CXXFLAGS) $(filter $*%, $(SOURCE_FILES)) > $@

$(OUTPUT_DIR)%.o:
	$(CXX) $(CXXFLAGS) -c $< -o $@

$(OUTPUT_DIR)%.a: $(OBJS)
	avr-ar -r $@ $(OBJS)

all: $(DEPENDANCIES) $(OBJS) $(LIBS)

$(OUTPUT_DIR)%.exec: $(@:.exec=.o) $(LIBS)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $(@:.exec=.o) -o $@ -lArduino -L$(OUTPUT_DIR)

#EXEC_LIST = $(foreach i,$(OBJS),$(shell (avr-nm $i | grep " T main$$" > /dev/null) && echo $(patsubst %.o,%.exec,$i)))
#t: EXEC_LIST = $(foreach i,$(OBJS),$(shell (avr-nm $i | grep " T main$$" > /dev/null) && echo $(patsubst %.o,%.exec,$i)))
t: $(OBJS)
	$(foreach i,$?, # $(shell echo '$i '))

#exec: $(LIBS) $(patsubst %.o,%.exec,$(shell for i in $(OBJS) ; do (avr-nm $$i | grep " T main$$" > /dev/null) && echo $$i ; done)) ;

tt:
	@echo $(subst :, ,$(PATH))

clean:
	rm -fr $(OUTPUT_DIR)

-include $(DEPENDANCIES)

#TARGET = testcpp
#SRCS   = dummy.cpp
#OBJS   = $(SRCS:.cc=.o)
#DEPS   = $(SRCS:.cc=.depends)

#$(OUTPUT):
#	mkdir -p $(OUTPUT)
#	echo $(OUTPUT)
#	echo $(asd)
#
#PREPARE: | $(OUTPUT)
#
#all: PREPARE #$(TARGET)

#$(TARGET): $(OBJS)
#	echo NNN $(CXX) $(CXXFLAGS) $(LDFLAGS) $(OBJS) -o $(TARGET)
#	echo NNNN $(CXX) $(CXXFLAGS) $(LDFLAGS) $(OBJS) -o $(OUTPUT)/$(TARGET)

#.cc.o:
#	echo MMM $(CXX) $(CXXFLAGS) -c $< -o $@
#	$(CXX) $(CXXFLAGS) -c $< -o $(OUTPUT)/$(basename $<).o
#
#deps: $(DEPS)
#
#%.depends: %.cc
#	echo DEPPPP $(CXX) -M $(CXXFLAGS) $< > $@
#

