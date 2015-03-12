.DEFAULT_GOAL = all

SOURCE_DIR := src/
OUTPUT_DIR := output/
LIB_NAME   := Arduino
LIB        := $(OUTPUT_DIR)lib$(LIB_NAME).a
ARDUINO_LIB_FOLDER := $(SOURCE_DIR)ArduinoLibrary/
INCLUDE_FOLDERS = $(dir $(wildcard $(SOURCE_DIR)*/) $(wildcard $(ARDUINO_LIB_FOLDER)*/))
INCLUDE = $(foreach dir,$(INCLUDE_FOLDERS),-I"$(dir)" )

MAIN_FILE  = arduino/main.cpp
MAIN_O     = $(OUTPUT_DIR)$(MAIN_FILE:.cpp=.o)
MAIN_EXEC  = $(OUTPUT_DIR)$(MAIN_FILE:.cpp=.exec)

CXX      = avr-gcc
# Idea borrowed from WString.h: added compiler options -felide-constructors -std=c++0x 
# -felide-constructors -std=c++0x
CXXFLAGS = $(INCLUDE) \
 -Wall -Os -fpack-struct -fshort-enums -ffunction-sections -fdata-sections -fno-keep-static-consts -fpermissive -g \
 -fno-threadsafe-statics -DARDUINO=158 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR \
 -std="c++11" -funsigned-char -funsigned-bitfields -fno-exceptions -mmcu=atmega328p -DF_CPU=16000000UL
LDFLAGS  = -Wl,--gc-sections -mrelax -mmcu=atmega328p -lm -lc

rwildcard=$(foreach d,$(notdir $(wildcard $1$2*)),$(call rwildcard,$1,$2$d/,$3) $(filter $(subst *,%,$3),$2$d))
SOURCE_FILES := $(call rwildcard, $(SOURCE_DIR),, *.cpp *.c)
OBJS         := $(addprefix $(OUTPUT_DIR), $(addsuffix .o, $(basename $(SOURCE_FILES))))

-include $(OBJS:.o=.d)

define c_build
@mkdir -p $(dir $@)
$(CXX) $(CXXFLAGS) -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@ $(@:%.o=%.d)" -c -o "$@" "$<"
@rm -f $(@:.o=.exec)
@((avr-nm $@ | grep " T loop$$" > /dev/null) && (echo $(patsubst %.o,%.elf,$@): $@>$(subst .o,.exec,$(@) $(MAIN_EXEC)))) & true
@((avr-nm $@ | grep " T main$$" > /dev/null) && (echo $(patsubst %.o,%.elf,$@): $@>$(subst .o,.exec,$(@)))) & true
endef

$(OUTPUT_DIR)%.o: $(SOURCE_DIR)%.c
	$(call c_build)

$(OUTPUT_DIR)%.o: $(SOURCE_DIR)%.cpp
	$(call c_build)

define obj_dump
	avr-size --format=avr --mcu=atmega328p "$(1)" > "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	avr-size --format=Berkeley --mcu=atmega328p "$(1)" >> "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	avr-objdump -Cs -j .data "$(1)" >> "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	echo "Print final symbols sizes (b - .bss; d - .data; t - .text; v/w - weak)" >> "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	avr-nm -td --size-sort -C "$(1)" |sort -f -k 3,5 >> "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	avr-objdump -h -C -S "$(1)" >> "$(2)"
endef

define lib_dump
	avr-size --format=Berkeley --mcu=atmega328p "$(1)" > "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	echo "Print final symbols sizes (b - .bss; d - .data; t - .text; v/w - weak)" >> "$(2)"
	echo ... >> "$(2)"
	echo ... >> "$(2)"
	avr-nm -td -C -S "$(1)" >> "$(2)"
endef

$(OUTPUT_DIR)%.elf: $(OUTPUT_DIR)%.o $(LIB)
	avr-g++ $(LDFLAGS) -L$(OUTPUT_DIR) -Wl,-Map,$(@:.elf=.map),--cref -o "$@" $(@:.elf=.o) \
		$(if $(shell (avr-nm $(@:.elf=.o) | grep " T loop$$" > /dev/null) && (echo EXEC)),$(MAIN_O)) \
		 -l$(LIB_NAME)
	@$(call obj_dump,$(@),$(@:.elf=.lss))
	avr-objcopy -R .eeprom -R .fuse -R .lock -R .signature -O ihex "$(@)" "$(@:.elf=.hex)"

ifeq ($(MAKECMDGOALS),all_exec)
EXEC_FILES := $(filter-out $(MAIN_EXEC),$(call rwildcard,,$(OUTPUT_DIR), *.exec))
LIB_OBJS := $(filter-out $(subst .exec,.o,$(EXEC_FILES) $(MAIN_EXEC)),$(OBJS))
-include $(EXEC_FILES)

$(LIB): $(LIB_OBJS)
	avr-ar -r $@ $(LIB_OBJS)
	@$(call lib_dump,$(LIB),$(LIB:.a=.lss))

.PHONY: all_exec
all_exec: $(LIB) $(EXEC_FILES:.exec=.elf);
endif

upload_file := $(firstword $(call rwildcard,,$(OUTPUT_DIR), */$(file).hex))
upload_files = $(sort $(basename $(notdir $(call rwildcard,,$(OUTPUT_DIR), *.hex))))

define list_hex_files
	echo "\\n"
	echo Upload a HEX file to Arduino using:
	echo "\\n"
	echo make upload file=[name of hex file]
	echo "\\n"
	echo Available hex files:
	echo ====================
	echo " $(foreach i,$(upload_files),$i\\n)"
endef

.PHONY: list
list:
	@$(call list_hex_files)

.PHONY: all
all: $(OBJS)
	make all_exec
	make list
	@echo All done.

.PHONY: clean
clean:
	rm -fr $(OUTPUT_DIR)

.PHONY: upload
upload:
	@$(if $(upload_file),\
	avrdude -pm328p -cstk500v1 -P/dev/ttyUSB0 -b57600 -D -Uflash:w:"$(upload_file)":a,\
	echo "\nHEX file not found\n"; $(call list_hex_files); false \
	)
