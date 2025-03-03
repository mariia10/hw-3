package mariia.budiak.practices.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mariia.budiak.practices.service.RegistryLocalHostInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RequestMapping(value = "local-mashine-register-info/", name = "считывание реестра непостредсвенно на компьютере")
@Api(tags = "считывание реестра непостредсвенно при запуске утилиты на компьютере")
@RestController
public class GetLocalMashineInfoController {
    @Autowired
    RegistryLocalHostInfoService registryService;

    private static final String DEFAULT_REGISTRY_PATH = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";
    private static final String DEFAULT_INTERFACE_PATH = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";


    @GetMapping("/userInformation")
    @ApiOperation(value = "Информация о пользователях системы")
    public List<String> getUserInformation(@RequestParam(value = "path", defaultValue = DEFAULT_REGISTRY_PATH) String registryPath) {
        return registryService.getUserInformation(registryPath);
    }


    @GetMapping("/networkInformation")
    @ApiOperation(value = "Вся информация о сетевом стеке")
    public List<String> getNetworkInformation(@RequestParam(value = "path", defaultValue = DEFAULT_INTERFACE_PATH) String registryPath) {


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


        return registryService.getNetInfo(registryPath);
    }

    @GetMapping("/operationSystemInfo")
    @ApiOperation(value = "Вся информация об операционной системе")
    public HashMap<String, List<String>> getOperationSystemInformation() {
        return registryService.getInformationSystemInfo();
    }

    @GetMapping("/biosInfo")
    @ApiOperation(value = "Вся информация о BIOS")
    public HashMap<String, List<String>> getBiosInformation() {
        return registryService.getBiosInfo();
    }

}




