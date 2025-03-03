package mariia.budiak.practices.service;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistryLocalHostInfoService {

    private static final Map<String, String[]> constOsValue = new HashMap<>();

    private static final int KEY_READ = 0x20019; // Определяем KEY_READ

    public static final int HKEY_LOCAL_MACHINE = 0x80000002;

    static {
        constOsValue.put("SYSTEM\\CurrentControlSet\\Control\\ComputerName", new String[]{"ComputerName"});
        constOsValue.put("SYSTEM\\CurrentControlSet\\Control\\Windows", new String[]{"ShutdownTime"});
        constOsValue.put("SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", new String[]{"ProductName", "EditionID", "DisplayVersion", "CurrentBuild", "UBR", "InstallDate", "RegisteredOwner"});
        constOsValue.put("SYSTEM\\CurrentControlSet\\Control\\TimeZoneInformation", new String[]{"TimeZone"});
    }


    public interface Advapi32 extends Library {
        Advapi32 INSTANCE = Native.load("advapi32", Advapi32.class);

        int RegOpenKeyExA(int hkey, String subKey, int ulOptions, int samDesired, int[] phkResult);

        int RegQueryValueExA(int hkey, String lpValueName, int[] lpReserved, int[] lpType, byte[] lpData, IntByReference lpcbData);

        void RegCloseKey(int hkey);
    }


    public List<String> getUserInformation(String registryPath) {
        List<String> users = new ArrayList<>();
        try {
            String[] userKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, registryPath);
            for (String userKey : userKeys) {
                String userPath = registryPath + "\\" + userKey;

                // Извлечение значений
                String profileImagePath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, userPath, "ProfileImagePath");
                String username = profileImagePath != null ? profileImagePath.replaceAll("C:\\\\Users\\\\", "") : "Unknown";

                String flags = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, userPath, "Flags") == null ? "No" : "Yes";
                String lastWriteTime = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, userPath, "LastWriteTime") == null ? null :
                        Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, userPath, "LastWriteTime").toString();

                // Формируем строку с информацией о пользователе
                String userInfo = String.format("User SID: %s, Username: %s, Profile Image Path: %s, Flags: %s, Last Write Time: %s",
                        userKey, username, profileImagePath, flags, lastWriteTime);
                users.add(userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<String> getNetInfo(String registryPath) {
        List<String> parameters = new ArrayList<>();
        // Перечень значений, которые мы хотим получить
        List<String> valuesToRetrieve = List.of(
                "DhcpIPAddress",
                "DhcpNameServer",
                "DhcpServer",
                "DhcpSubnetMask",
                "Domain",
                "DhcpDefaultGateway"
        );

        // Получаем и выводим значения
        for (String valueName : valuesToRetrieve) {
            try {
                String value = Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_LOCAL_MACHINE,
                        registryPath,
                        valueName
                );
                parameters.add(valueName + ": " + value);
            } catch (RuntimeException e) {
                parameters.add(valueName + " not found. Error: " + e.getMessage());

            }
        }
        return parameters;
    }

    public HashMap<String, List<String>> getInformationSystemInfo() {
        var resultMap = new HashMap<String, List<String>>();
        /*
        String[] paths = {
                "SYSTEM\\CurrentControlSet\\Control\\ComputerName",
                "SYSTEM\\CurrentControlSet\\Control\\Windows",
                "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
                "SYSTEM\\CurrentControlSet\\Control\\TimeZoneInformation"
        };

        String[] values = {
                "ComputerName", "ShutdownTime",
                "ProductName", "EditionID", "DisplayVersion",
                "CurrentBuild", "UBR", "InstallDate", "RegisteredOwner",
                "TimeZone"
        };



        for (String path : paths) {
            for (String value : values) {
                String result = getRegistryValue(path, value);
                if (result != null) {
                    System.out.println(path + "\\" + value + ": " + result);
                }
            }
        }
  */
        for (Map.Entry<String, String[]> entry : constOsValue.entrySet()) {
            String keyPath = entry.getKey();
            String[] values = entry.getValue();

            resultMap.put(keyPath, new ArrayList<>());
            for (String valueName : values) {
                try {
                    String value = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, keyPath, valueName);
                    resultMap.get(keyPath).add(valueName + ": " + value);
                } catch (Exception e) {
                    resultMap.get(keyPath).add(("Error reading value: " + valueName + " from " + keyPath));
                }
            }
        }
        return resultMap;
    }

    public static String getRegistryValue(String subKey, String valueName) {
        int[] hKeyResult = new int[1];
        int openResult = Advapi32.INSTANCE.RegOpenKeyExA(HKEY_LOCAL_MACHINE, subKey, 0, 0x20019 /* KEY_READ */, hKeyResult);
        if (openResult != 0) {
            System.err.println("Failed to open registry key: " + openResult);
            return null;
        }

        int[] type = new int[1];
        byte[] data = new byte[1024]; // buffer for the data
        IntByReference dataSize = new IntByReference(data.length);

        int queryResult = Advapi32.INSTANCE.RegQueryValueExA(hKeyResult[0], valueName, null, type, data, dataSize);

        Advapi32.INSTANCE.RegCloseKey(hKeyResult[0]); // Closing the registry key

        if (queryResult != 0) {
            System.err.println("Failed to query registry value: " + queryResult);
            return null;
        }

        return Native.toString(data);
    }

    public HashMap<String, List<String>> getBiosInfo() {
        var resultMap = new HashMap<String, List<String>>();
        // Словарь с путями реестра и и ключами
        Map<String, List<String>> constBiosValue = new HashMap<>();
        constBiosValue.put("HARDWARE\\DESCRIPTION\\System",
                List.of("SystemBiosVersion", "SystemBiosDate", "VideoBiosVersion"));
        constBiosValue.put("HARDWARE\\DESCRIPTION\\System\\BIOS",
                List.of("BIOSVendor", "BIOSVersion", "BIOSReleaseDate"));
        constBiosValue.put("SYSTEM\\CurrentControlSet\\Control\\SystemInformation",
                List.of("BIOSVersion", "BIOSReleaseDate"));

        // Чтение значений из реестра
        for (Map.Entry<String, List<String>> entry : constBiosValue.entrySet()) {
            String registryPath = entry.getKey();
            List<String> keys = entry.getValue();
            resultMap.put(registryPath, new ArrayList<>());
            try {
                var hKey = Advapi32Util.registryGetKey(
                        WinReg.HKEY_LOCAL_MACHINE,
                        registryPath,
                        KEY_READ);

                for (String key : keys) {
                    try {
                        String value = Advapi32Util.registryGetStringValue(hKey.getValue(), key);
                        resultMap.get(registryPath).add(key + ": " + value);
                    } catch (Exception e) {
                        resultMap.get(registryPath).add(key + ": error " + e.getMessage());
                    }
                }
                Advapi32Util.registryCloseKey(hKey.getValue());
            } catch (Exception e) {
                System.out.println("Error reading registry path " + registryPath + ": " + e.getMessage());
            }
        }
        return resultMap;
    }

    public List<String> getHardWareInfo() {

        var resultList = new ArrayList<String>();

        // Определяем ключи реестра для получения информации о системе
        String systemManufacturerKey = "SYSTEM\\CurrentControlSet\\Control\\SystemInformation";
        String systemProductNameKey = "SYSTEM\\CurrentControlSet\\Control\\SystemInformation";
        String driverDescKey = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\0000";

        // Определяем ключи для процессора
        String cpuKey = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0"; // Используем 0 для первого процессора
        String scsiKey = "HARDWARE\\DEVICEMAP\\Scsi";

        // Получаем значения из реестра
        try {
            // Чтение информации о системе
            String systemManufacturer = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, systemManufacturerKey, "SystemManufacturer");
            resultList.add("System Manufacturer: " + systemManufacturer);

        } catch (Exception e) {
            resultList.add("System Manufacturer: " + e.getMessage());

        }
        try {
            String systemProductName = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, systemProductNameKey, "SystemProductName");
            resultList.add("System Product Name: " + systemProductName);

        } catch (Exception ex) {
            resultList.add("System Product Name: " + ex.getMessage());

        }
        try {
            String driverDesc = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, driverDescKey, "DriverDesc");
            resultList.add("Driver Description: " + driverDesc);

        } catch (Exception ex) {
            resultList.add("Driver Description: " + ex.getMessage());

        }
        try {
            String processorName = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, cpuKey, "ProcessorNameString");
            resultList.add("Processor Name: " + processorName);
        } catch (Exception e) {
            resultList.add("Processor Name: " + e.getMessage());

        }
        try {
            String identifier = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, cpuKey, "Identifier");
            resultList.add("Processor Identifier: " + identifier);
        } catch (Exception ex) {
            resultList.add("Processor Identifier: " + ex.getMessage());

        }
        try {
            String vendorIdentifier = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, cpuKey, "VendorIdentifier");
            resultList.add("Vendor Identifier: " + vendorIdentifier);

        } catch (Exception ex) {
            resultList.add("Vendor Identifier: " + ex.getMessage());
        }

        try {
            String mhz = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, cpuKey, "~MHz");
            resultList.add("Clock Speed (MHz): " + mhz);
        } catch (Exception e) {
            resultList.add("Clock Speed (MHz): " + e.getMessage());
        }
        try {
            String scsiIdentifier = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, scsiKey, "Identifier");
            resultList.add("SCSI Identifier: " + scsiIdentifier);
        } catch (Exception e) {
            resultList.add("SCSI Identifier: " + e.getMessage());

        }
        try {
            String serialNumber = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE, scsiKey, "SerialNumber");

            resultList.add("SCSI Serial Number: " + serialNumber);

        } catch (Exception ex) {
            resultList.add("SCSI Serial Number: " + ex.getMessage());

        }

        return resultList;
    }

    public List<String> getSoftWareInfo() {
        List<String> registryEntries = new ArrayList<>();
        String registryPathLocalMachine = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
        String registryPathCurrentUser = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";

        addInstalledProgramsFromRegistry(registryPathLocalMachine, registryEntries, WinReg.HKEY_LOCAL_MACHINE);

        addInstalledProgramsFromRegistry(registryPathCurrentUser, registryEntries, WinReg.HKEY_CURRENT_USER);

        return registryEntries;
    }

    private void addInstalledProgramsFromRegistry(String registryPath, List<String> registryEntries, WinReg.HKEY hive) {
        String[] subKeys = Advapi32Util.registryGetKeys(hive, registryPath);
        for (String subKey : subKeys) {
            try {
                var valueNames = Advapi32Util.registryGetValues(hive, registryPath + "\\" + subKey);
                String programName = null;
                String vendor = null;

                // Извлекаем название программы и вендора
                if (valueNames.containsKey("DisplayName")) {
                    programName = Advapi32Util.registryGetStringValue(hive, registryPath + "\\" + subKey, "DisplayName");
                }
                if (valueNames.containsKey("Publisher")) {
                    vendor = Advapi32Util.registryGetStringValue(hive, registryPath + "\\" + subKey, "Publisher");
                }

                // Если программа имеет название, добавляем информацию в список
                if (programName != null) {
                    registryEntries.add("Program: " + programName + " | Vendor: " + (vendor != null ? vendor : "N/A"));
                }
            } catch (Exception e) {
                System.err.println("Error accessing the registry: " + e.getMessage());
            }
        }
    }


}






