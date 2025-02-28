package mariia.budiak.practices.controller;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import mariia.budiak.practices.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/networkInformation")
    public List<String> getNetworkInformation() {
        List<String> devices = new ArrayList<>();
        String registryPath = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4D36E972-E325-11CE-BFC1-08002BE10318}";

        try {
            String[] subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, registryPath);
            for (String subKey : subKeys) {
                String deviceName = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, registryPath + "\\" + subKey, "DriverDesc")!=null?
                        Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, registryPath + "\\" + subKey, "DriverDesc").toString(): null;
                if (deviceName != null) {
                    devices.add(deviceName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return devices;
    }
}
