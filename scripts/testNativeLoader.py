#!/usr/bin/env python3
"""Run isolated bootstrap regression tests on JDK 21, without generated server sources."""
import hashlib
import os
from pathlib import Path
import subprocess
import tempfile
import urllib.request

ROOT = Path(__file__).resolve().parent.parent
ARTIFACTS = {
    "org/junit/platform/junit-platform-console-standalone/1.12.2/junit-platform-console-standalone-1.12.2.jar":
        "329bd10288875a74d04c9ca6b7c9889c265ddf87b21a9ea42a7f7392f391472a",
    "org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar":
        "7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832",
}


def main():
    with tempfile.TemporaryDirectory(prefix="lattice-loader-test-") as work:
        work = Path(work)
        jars = []
        for artifact, digest in ARTIFACTS.items():
            data = urllib.request.urlopen("https://repo.maven.apache.org/maven2/" + artifact, timeout=60).read()
            if hashlib.sha256(data).hexdigest() != digest:
                raise RuntimeError("Dependency checksum mismatch: " + artifact)
            jar = work / artifact.rsplit("/", 1)[-1]
            jar.write_bytes(data)
            jars.append(str(jar))
        classes = work / "classes"
        classes.mkdir()
        sources = []
        for name in ("LatticeNativeLoader", "NativeLibraryCache"):
            for tree, suffix in (("main", ""), ("test", "TestSuite")):
                sources.append(str(ROOT / f"lattice-server/src/{tree}/java/com/latticemc/lattice/bootstrap/{name}{suffix}.java"))
        subprocess.run(["javac", "--release", "21", "-cp", os.pathsep.join(jars), "-d", str(classes), *sources], check=True)
        command = ["java"]
        if library := os.environ.get("LATTICE_TEST_NATIVE_LIBRARY"):
            command.append("-Dlattice.test.nativeLibrary=" + str(Path(library).resolve()))
        subprocess.run([*command, "-jar", jars[0], "execute", "--class-path", os.pathsep.join([str(classes), jars[1]]),
                        "--select-class", "com.latticemc.lattice.bootstrap.LatticeNativeLoaderTestSuite",
                        "--select-class", "com.latticemc.lattice.bootstrap.NativeLibraryCacheTestSuite",
                        "--details", "summary", "--fail-if-no-tests"], check=True)


if __name__ == "__main__":
    main()
