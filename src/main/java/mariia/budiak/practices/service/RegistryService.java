package mariia.budiak.practices.service;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Service;
import com.sun.jna.ptr.PointerByReference;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinNT.KEY_READ;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

@Service
public class RegistryService {
    @PostConstruct
    public void init(){
        getUserInformation();
    }




    private static final int KEY_READ = 0x20019; // Права доступа для чтения (HKEY_READ)
    private static final int HKEY_LOCAL_MACHINE = 0x80000002; // Корневой ключ реестра

    public List<String> getUserInformation() {
        List<String> users = new ArrayList<>();
        var hKey = new WinReg.HKEYByReference();


        // Открыть ключ, содержащий информацию о пользователях. Измените путь в зависимости от системы
        String userKeyPath = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";

        if (Advapi32.INSTANCE.RegOpenKeyEx(WinReg.HKEY_LOCAL_MACHINE, userKeyPath, 0, KEY_READ, hKey) == 0) {
            int index = 0;
            IntByReference lpcbName = new IntByReference();
            byte[] lpName = new byte[256];

            // Перебираем ключи, чтобы получить информацию о пользователях
            while (Advapi32.INSTANCE.RegEnumKeyEx(hKey.getValue(), index, lpName, lpcbName, null, null, null, null) == 0) {
                String userSid = Native.toString(lpName, String.valueOf(0));
                users.add(userSid);
                index++;
            }

            // Закрываем ключ после обработки
            Advapi32.INSTANCE.RegCloseKey(hKey.getValue());
        } else {
            System.out.println("Не удалось открыть ключ реестра");
        }

        return users;
    }
}

    // Добавьте аналогичные методы для других типов информации
