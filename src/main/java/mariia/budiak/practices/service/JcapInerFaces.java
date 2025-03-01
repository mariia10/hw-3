package mariia.budiak.practices.service;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import org.springframework.stereotype.Service;
import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

;
@Service
public class JcapInerFaces {
    private static final int KEY_READ = 0x20019; // Определяем KEY_READ



        public List<String> getInfos() {
            List<String> guids = new ArrayList<>();
            String registryPath = "SYSTEM\\CurrentControlSet\\Control\\Class";
            try {
                WinReg.HKEYByReference hKey = new WinReg.HKEYByReference();
                int result = Advapi32.INSTANCE.RegOpenKeyEx(
                        WinReg.HKEY_LOCAL_MACHINE,
                        registryPath,
                        0,
                        KEY_READ,
                        hKey
                );

                if (result != 0) {
                    throw new RuntimeException("Failed to open registry key: " + result);
                }

                // Прочитаем все значения под этим ключом
                String[] subKeys = Advapi32Util.registryGetKeys(hKey.getValue());
                //   System.out.println("SubKey: " + subKey);
                // Вы можете также читать значения, если это необходимо
                guids.addAll(Arrays.asList(subKeys));

                // Закрываем хендл
                Advapi32.INSTANCE.RegCloseKey(hKey.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return guids;
        }
}
