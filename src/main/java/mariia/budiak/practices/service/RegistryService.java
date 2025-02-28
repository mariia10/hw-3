package mariia.budiak.practices.service;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistryService {


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
}

// Добавьте аналогичные методы для других типов информации
