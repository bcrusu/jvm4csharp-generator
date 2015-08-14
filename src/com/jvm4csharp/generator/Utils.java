package com.jvm4csharp.generator;

import java.util.Set;

class Utils {
    public static boolean hasOtherPackageReferences(String[] includedPackages, Set<String> referencedPackages) {
        for (String referencedPackage : referencedPackages) {
            boolean ok = false;
            for (String item : includedPackages) {
                if (referencedPackage.startsWith(item)) {
                    ok = true;
                    break;
                }
            }

            if (!ok)
                return true;
        }

        return false;
    }
}
