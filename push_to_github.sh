#!/bin/bash
# Script de cap nhat thay doi len GitHub

# Kiem tra xem co cung cap commit message khong
if [ -z "$1" ]; then
    echo "Loi: Vui long cung cap mot tin nhan commit."
    echo "Vi du: ./push_to_github.sh \"Sua loi GUI\""
    exit 1
fi

echo "--- Them tat ca thay doi vao Git ---"
git add .

echo "--- Tao commit voi tin nhan: '$1' ---"
git commit -m "$1"

echo "--- Day thay doi len nhanh 'main' cua GitHub ---"
git push origin main

echo "--- HOAN TAT ---"
