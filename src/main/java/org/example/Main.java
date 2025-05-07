package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        removeRule();
        createFirewallRule();
    }

    private static void createFirewallRule() throws IOException {

        List<String> list = new ArrayList<>();

        try {
            // Получаем список всех IP-адресов для заданного DNS-имени
            InetAddress[] addresses = InetAddress.getAllByName("account.jetbrains.com");

            for (InetAddress addr : addresses) {
                System.out.println(addr.getHostAddress());
                list.add(addr.getHostAddress());
            }

        } catch (UnknownHostException ex) {
            System.err.println("Ошибка разрешения DNS: " + ex.getMessage());
        }

        InputStream originalIS = Main.class.getResourceAsStream("/add.ps1");
        File temp = File.createTempFile("add", ".ps1");
        temp.deleteOnExit();
        Files.copy(originalIS, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ProcessBuilder pb = new ProcessBuilder(
                "powershell",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                temp.getAbsolutePath(),
                "-ips",
                String.join(",", list)
        );

        execute(pb);
    }

    private static void removeRule() throws IOException {

        InputStream originalIS = Main.class.getResourceAsStream("/remove.ps1");
        File temp = File.createTempFile("remove", ".ps1");
        temp.deleteOnExit();
        Files.copy(originalIS, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ProcessBuilder pb = new ProcessBuilder(
                "powershell",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                temp.getAbsolutePath()
        );
        execute(pb);
    }

    private static void execute(ProcessBuilder pb) {
        pb.environment().put("PROCESSOR_ARCHITECTURE", "AMD64");
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                System.out.println("Сценарий успешно выполнен.");
            } else {
                System.err.println("Возникла ошибка при выполнении сценария PowerShell.");
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}