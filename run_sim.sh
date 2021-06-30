#!/bin/bash

abort()
{
    echo >&2 '
***************
*** ABORTED ***
***************
'
    echo "Exiting program." >&2
    [ -e OUTPUT.txt ] && rm OUTPUT.txt 
    cd ..
    exit 1
}

trap 'abort' 0

set -e

cd src

# Delete temp file if it exists
[ -e OUTPUT.txt ] && rm OUTPUT.txt 

touch OUTPUT.txt

echo "Compiling Java program."

javac *.java

echo "Running Java program."

java App ${@:1}

echo "Plotting results."


[ -e OUTPUT.txt ] && rm OUTPUT.txt 

cd ..

trap : 0

echo >&2 '
************
*** DONE *** 
************
'
