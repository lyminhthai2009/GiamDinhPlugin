#!/bin/bash

# ANSI color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# --- Ham xac nhan ---
read -p "$(echo -e ${YELLOW}"Ban co muon chay 'mvn clean' de don dep du an truoc khi build? (y/n): "${NC})" -n 1 -r
echo # Move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo -e "${CYAN}Dang don dep du an...${NC}"
    mvn clean
fi

# --- 1. Bien dich du an ---
echo -e "\n${CYAN}--- Bat dau bien dich du an voi 'mvn package' ---${NC}"
mvn package

# Kiem tra ket qua bien dich
if [ $? -ne 0 ]; then
    echo -e "${RED}Bien dich that bai! Vui long kiem tra loi o tren.${NC}"
    exit 1
fi
echo -e "${GREEN}Bien dich thanh cong!${NC}"

# --- 2. Tim file .jar ---
JAR_FILE=$(find target -name "GiamDinh-*.jar" ! -name "*-original.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}Khong tim thay file .jar da bien dich trong thu muc 'target'.${NC}"
    exit 1
fi
echo -e "Tim thay file JAR: ${GREEN}$JAR_FILE${NC}"


# --- 3. Nen thu muc nguon ---
echo -e "\n${CYAN}--- Dang nen toan bo thu muc du an... ---${NC}"
cd .. # Di ra khoi thu muc GiamDinh de nen no
zip -r GiamDinh_Source.zip GiamDinh/ -x "GiamDinh/target/*" -x "GiamDinh/libs/*"
cd GiamDinh # Quay tro lai thu muc GiamDinh

if [ $? -ne 0 ]; then
    echo -e "${RED}Nen thu muc that bai!${NC}"
    exit 1
fi
echo -e "Da nen thanh cong: ${GREEN}../GiamDinh_Source.zip${NC}"


# --- 4. Di chuyen file ---
BACKUP_DIR="/sdcard/Download/GiamDinh_Backup"
echo -e "\n${CYAN}--- Dang di chuyen file den thu muc backup ---${NC}"
echo -e "Thu muc dich: ${YELLOW}$BACKUP_DIR${NC}"

# Yeu cau quyen truy cap bo nho
termux-setup-storage

# Tao thu muc backup neu chua co
mkdir -p "$BACKUP_DIR"

# Di chuyen file .jar
mv "$JAR_FILE" "$BACKUP_DIR/"
if [ $? -eq 0 ]; then
    echo -e "Da di chuyen ${GREEN}$(basename $JAR_FILE)${NC} thanh cong."
else
    echo -e "${RED}Di chuyen file .jar that bai!${NC}"
fi

# Di chuyen file .zip
mv ../GiamDinh_Source.zip "$BACKUP_DIR/"
if [ $? -eq 0 ]; then
    echo -e "Da di chuyen ${GREEN}GiamDinh_Source.zip${NC} thanh cong."
else
    echo -e "${RED}Di chuyen file .zip that bai!${NC}"
fi


echo -e "\n${GREEN}--- HOAN TAT! ---${NC}"
echo -e "File .jar va ma nguon .zip da duoc luu tai: ${YELLOW}$BACKUP_DIR${NC}"
