package mariia.budiak.practices.service;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Advapi32 extends Library {
        Advapi32 INSTANCE = Native.load("advapi32", Advapi32.class);

        int RegOpenKeyEx(int hKey, String lpSubKey, int ulOptions, int samDesired, WinReg.HKEYByReference hkey);
        int RegEnumKeyEx(int hKey, int dwIndex, byte[] lpName, IntByReference lpcbName,
                         int[] lpdwReserved, byte[] lpClass, IntByReference lpcbClass,
                         IntByReference lpftLastWriteTime);
        int RegCloseKey(int hKey);
        int RegQueryValueEx(int hKey, String lpValueName, int[] lpType, byte[] lpData,
                            IntByReference lpcbData);
    }