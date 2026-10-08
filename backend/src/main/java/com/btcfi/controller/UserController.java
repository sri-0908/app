package com.btcfi.controller;

import com.btcfi.dto.UserPositionDTO;
import com.btcfi.model.User;
import com.btcfi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/connect")
    public ResponseEntity<User> connectWallet(@RequestBody java.util.Map<String, String> body) {
        String walletAddress = body.get("walletAddress");
        return ResponseEntity.ok(userService.registerOrGetUser(walletAddress));
    }

    @GetMapping("/{id}/position")
    public ResponseEntity<UserPositionDTO> getUserPosition(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserPosition(id));
    }
}
