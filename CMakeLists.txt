cmake_minimum_required(VERSION 3.22.1)
project("solauncher")

add_library(solauncher SHARED solauncher.cpp)

find_library(log-lib log)

target_link_libraries(solauncher
    ${log-lib}
    dl
)
