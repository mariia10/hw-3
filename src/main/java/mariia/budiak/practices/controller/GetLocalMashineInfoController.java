package mariia.budiak.practices.controller;

import mariia.budiak.practices.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
