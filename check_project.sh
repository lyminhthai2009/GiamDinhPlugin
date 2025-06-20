#!/bin/bash

# ANSI color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "[ ${GREEN}OK${NC} ] Tim thay file: $1"
    else
        echo -e "[ ${RED}THIEU${NC} ] Khong tim thay file: $1"
        MISSING_FILES=true
    fi
}

# Function to check if a directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "[ ${GREEN}OK${NC} ] Tim thay thu muc: $1"
    else
        echo -e "[ ${RED}THIEU${NC} ] Khong tim thay thu muc: $1"
        MISSING_FILES=true
    fi
}


MISSING_FILES=false
echo -e "${YELLOW}--- Bat dau kiem tra du an GiamDinh ---${NC}"

# --- 1. Kiem tra cau truc thu muc ---
echo -e "\n${YELLOW}Kiem tra cau truc thu muc...${NC}"
check_dir "libs"
check_dir "src/main/java/com/yogurt/giamdinh/commands/subcommands"
check_dir "src/main/java/com/yogurt/giamdinh/gui"
check_dir "src/main/java/com/yogurt/giamdinh/listeners"
check_dir "src/main/java/com/yogurt/giamdinh/managers"
check_dir "src/main/java/com/yogurt/giamdinh/model"
check_dir "src/main/java/com/yogurt/giamdinh/utils"
check_dir "src/main/java/com/yogurt/giamdinh/hooks"
check_dir "src/main/resources"

# --- 2. Kiem tra cac file cau hinh .yml ---
echo -e "\n${YELLOW}Kiem tra cac file cau hinh (.yml)...${NC}"
check_file "pom.xml"
check_file "src/main/resources/plugin.yml"
check_file "src/main/resources/config.yml"
check_file "src/main/resources/messages_vi.yml"
check_file "src/main/resources/ores.yml"
check_file "src/main/resources/levels.yml"
check_file "src/main/resources/boosters.yml"
check_file "src/main/resources/tools.yml"
check_file "src/main/resources/shop.yml"
check_file "src/main/resources/player_stats_gui.yml"


# --- 3. Kiem tra cac file ma nguon .java ---
echo -e "\n${YELLOW}Kiem tra cac file ma nguon (.java)...${NC}"
# Main file
check_file "src/main/java/com/yogurt/giamdinh/GiamDinh.java"
# Utils
check_file "src/main/java/com/yogurt/giamdinh/utils/ChatUtil.java"
check_file "src/main/java/com/yogurt/giamdinh/utils/ItemUtil.java"
# Models
check_file "src/main/java/com/yogurt/giamdinh/model/PlayerData.java"
check_file "src/main/java/com/yogurt/giamdinh/model/LevelPerk.java"
check_file "src/main/java/com/yogurt/giamdinh/model/Booster.java"
# Managers
check_file "src/main/java/com/yogurt/giamdinh/managers/ConfigManager.java"
check_file "src/main/java/com/yogurt/giamdinh/managers/PlayerManager.java"
check_file "src/main/java/com/yogurt/giamdinh/managers/EconomyManager.java"
check_file "src/main/java/com/yogurt/giamdinh/managers/LevelManager.java"
check_file "src/main/java/com/yogurt/giamdinh/managers/AppraisalManager.java"
check_file "src/main/java/com/yogurt/giamdinh/managers/GUIManager.java"
# GUI
check_file "src/main/java/com/yogurt/giamdinh/gui/BaseGUI.java"
check_file "src/main/java/com/yogurt/giamdinh/gui/AppraisalGUI.java"
check_file "src/main/java/com/yogurt/giamdinh/gui/PlayerStatsGUI.java"
check_file "src/main/java/com/yogurt/giamdinh/gui/ShopGUI.java"
# Listeners
check_file "src/main/java/com/yogurt/giamdinh/listeners/PlayerListener.java"
check_file "src/main/java/com/yogurt/giamdinh/listeners/GUIListener.java"
# Hooks
check_file "src/main/java/com/yogurt/giamdinh/hooks/GiamDinhPlaceholders.java"
# Commands
check_file "src/main/java/com/yogurt/giamdinh/commands/CommandManager.java"
check_file "src/main/java/com/yogurt/giamdinh/commands/SubCommand.java"
check_file "src/main/java/com/yogurt/giamdinh/commands/subcommands/ReloadSubCommand.java"
check_file "src/main/java/com/yogurt/giamdinh/commands/subcommands/SetLevelSubCommand.java"
check_file "src/main/java/com/yogurt/giamdinh/commands/subcommands/GiveSubCommand.java"

# --- 4. Kiem tra cac file dependencies .jar ---
echo -e "\n${YELLOW}Kiem tra cac file dependencies (.jar) trong 'libs/'...${NC}"
check_file "libs/paper.jar"
check_file "libs/vault.jar"
check_file "libs/placeholderapi.jar"
check_file "libs/mmoitems.jar"
check_file "libs/mythiclib.jar"
check_file "libs/playerpoints.jar"
check_file "libs/coinengine.jar"
check_file "libs/citizens.jar"
check_file "libs/itemsadder.jar"

# --- Tong ket ---
echo -e "\n${YELLOW}--- Kiem tra hoan tat ---${NC}"
if [ "$MISSING_FILES" = true ]; then
    echo -e "${RED}Phat hien co file bi thieu! Vui long kiem tra lai cac file duoc danh dau [THIEU] o tren.${NC}"
else
    echo -e "${GREEN}Chuc mung! Du an cua ban co ve da day du tat ca cac file can thiet.${NC}"
fi
