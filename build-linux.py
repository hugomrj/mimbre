#!/usr/bin/env python3

import shutil
import subprocess
from pathlib import Path

PROJECT_NAME = "Desk"
JAR_NAME = "mimbre-0.1.jar"
MAIN_CLASS = "com.app.Application"

def run(command):
    print(f"\n>>> {' '.join(command)}\n")
    subprocess.run(command, check=True)

def main():

    run(["./mvnw", "clean", "package", "-Plinux"])

    release_dir = Path("release")

    if release_dir.exists():
        shutil.rmtree(release_dir)

    run([
        "jpackage",
        "--name", PROJECT_NAME,
        "--input", "target",
        "--main-jar", JAR_NAME,
        "--main-class", MAIN_CLASS,
        "--type", "app-image",
        "--dest", "release"
    ])

    print("\nBuild Linux finalizado.")
    print(f"Ejecutable: release/{PROJECT_NAME}/bin/{PROJECT_NAME}")

if __name__ == "__main__":
    main()

