package com.valensas.undetected.chrome.driver.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Architecture;
import java.util.Arrays;
import org.slf4j.Logger;

public enum OSArchitecture {
  X86("x86", ArchitectureGroup.X86),
  I686("i686", ArchitectureGroup.X86),
  AMD64("amd64", ArchitectureGroup.X64),
  X86_64("x86_64", ArchitectureGroup.X64),
  AARCH64("aarch64", ArchitectureGroup.ARM64),
  ARM64("arm64", ArchitectureGroup.ARM64),
  M1("m1", ArchitectureGroup.ARM64),
  ARM("arm", ArchitectureGroup.ARM32),
  ARMV6L("armv6l", ArchitectureGroup.ARM32),
  ARMV7L("armv7l", ArchitectureGroup.ARM32),
  IA64("ia64", ArchitectureGroup.OTHER),
  SPARC("sparc", ArchitectureGroup.OTHER),
  SPARCV9("sparcv9", ArchitectureGroup.OTHER),
  PPC("ppc", ArchitectureGroup.OTHER),
  PPC64("ppc64", ArchitectureGroup.OTHER),
  PPC64LE("ppc64le", ArchitectureGroup.OTHER),
  MIPS("mips", ArchitectureGroup.OTHER),
  MIPS64("mips64", ArchitectureGroup.OTHER),
  MIPS64EL("mips64el", ArchitectureGroup.OTHER),
  RISCV64("riscv64", ArchitectureGroup.OTHER),
  S390("s390", ArchitectureGroup.OTHER),
  S390X("s390x", ArchitectureGroup.OTHER),
  ALPHA("alpha", ArchitectureGroup.OTHER),
  HPPA("hppa", ArchitectureGroup.OTHER),
  UNKNOWN("unknown", ArchitectureGroup.UNKNOWN);

  private final String value;
  private final ArchitectureGroup group;

  OSArchitecture(String value, ArchitectureGroup group) {
    this.value = value;
    this.group = group;
  }

  private ArchitectureGroup getGroup() {
    return group;
  }

  private enum ArchitectureGroup {
    X86,
    X64,
    ARM32,
    ARM64,
    OTHER,
    UNKNOWN
  }

  private static OSArchitecture fromString(String arch) {
    return Arrays.stream(values())
        .filter(osArch -> osArch.value.equalsIgnoreCase(arch))
        .findFirst()
        .orElse(UNKNOWN);
  }

  private static OSArchitecture get() {
    return fromString(System.getProperty("os.arch"));
  }

  public static WebDriverManager setWdmArchitecture(WebDriverManager manager, Logger logger) {
    OSArchitecture arch = OSArchitecture.get();
    ArchitectureGroup group = arch.getGroup();

    if (logger != null) {
      logger.info("Arch: {}, Group: {}", arch, group);
    }

    switch (group) {
      case X86:
        manager.architecture(Architecture.X32);
        break;
      case X64:
        manager.architecture(Architecture.X64);
        break;
      case ARM64:
        manager.architecture(Architecture.ARM64);
        manager.arm64();
        if (arch == AARCH64) {
          manager.arch64();
        }
        break;
      case ARM32:
        manager.architecture(Architecture.DEFAULT);
        manager.arch32();
        break;
      case UNKNOWN, OTHER:
        manager.architecture(Architecture.DEFAULT);
        break;
    }

    return manager;
  }
}
