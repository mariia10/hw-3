package mariia.budiak.practices.controller;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import mariia.budiak.practices.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sun.jna.ptr.IntByReference;

import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.List;
@RequestMapping("local-mashine-register-info/")
@RestController
public class GetLocalMashineInfoController {
    @Autowired
    RegistryService registryService;

    private static final String DEFAULT_REGISTRY_PATH = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";

    @GetMapping("/userInformation")
    public List<String> getUserInformation(@RequestParam(value = "path", defaultValue = DEFAULT_REGISTRY_PATH) String registryPath) {
       return registryService.getUserInformation(registryPath);
    }

    private static final int KEY_READ = 0x20019; // Определяем KEY_READ


    @GetMapping("/networkInformation")
    public List<String> getNetworkInformation() {

        List<String> parameters = new ArrayList<>();
        // Путь к ключу реестра, где хранятся сетевые устройства
       /*
        String registryPath = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4D36E972-E325-11CE-BFC1-08002BE10318}";
        try {
            // Получаем список всех устройств в классе сетевых адаптеров
            String[] subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, registryPath);
            for (String subKey : subKeys) {
                // Читаем значение каждого подключения
                String deviceName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, registryPath + "\\" + subKey, "DriverDesc");

                //   String macAddress = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, registryPath + "\\" + subKey, "NetworkAddress");

                parameters.add("Device Name: " + deviceName + ", MAC Address: ");
            }
        } catch (Exception e) {
            System.err.println("Не удалось открыть ключ реестра: " + e.getMessage());
        }
        return parameters;

        */
        String keyPath = "SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Interfaces\\{32a0d524-54cb-44fd-91e0-82c4a74dc41c}";

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
                        keyPath,
                        valueName
                );
                System.out.println(valueName + ": " + value);
            } catch (RuntimeException e) {
                System.out.println(valueName + " not found. Error: " + e.getMessage());

            }
        }
        return parameters;
    }


    }




