import os
import subprocess
import sys
import platform
import time

def print_step(msg):
    print(f"\n[{time.strftime('%H:%M:%S')}] ---> {msg}")

def print_error(msg):
    print(f"\n[{time.strftime('%H:%M:%S')}] [ERROR] {msg}")

def print_success(msg):
    print(f"\n[{time.strftime('%H:%M:%S')}] [SUCCESS] {msg}")

def run_command(cmd, exit_on_error=True):
    print(f"执行命令: {cmd}")
    result = subprocess.run(cmd, shell=True)
    if result.returncode != 0:
        print_error(f"命令执行失败，错误码: {result.returncode}")
        if exit_on_error:
            sys.exit(result.returncode)
    return result

def check_adb_devices():
    try:
        result = subprocess.check_output("adb devices", shell=True).decode('utf-8')
        lines = result.strip().split('\n')
        devices = []
        for line in lines[1:]:
            line = line.strip()
            if line and not line.endswith('offline'):
                parts = line.split()
                if len(parts) >= 2 and parts[1] == 'device':
                    devices.append(parts[0])
        return devices
    except Exception as e:
        return []

def main():
    print_step("一键启动 Maomaomao (Campus Cat App)")
    
    is_windows = platform.system() == 'Windows'
    gradlew_cmd = ".\\gradlew.bat" if is_windows else "./gradlew"
    
    if not os.path.exists(gradlew_cmd.strip(".\\")):
        print_error("未找到 gradlew 脚本，请确认您在项目的根目录下执行此脚本。")
        sys.exit(1)
    
    print_step("正在编译 Debug APK...")
    build_cmd = f"{gradlew_cmd} assembleDebug"
    run_command(build_cmd)
    
    apk_path = os.path.join("app", "build", "outputs", "apk", "debug", "app-debug.apk")
    if not os.path.exists(apk_path):
        print_error(f"未找到生成的 APK 文件: {apk_path}")
        sys.exit(1)
        
    print_success("APK 编译成功！")
    
    print_step("检查已连接的设备或模拟器...")
    devices = check_adb_devices()
    if not devices:
        print_error("未找到任何已连接的 Android 设备或模拟器。")
        print("请启动 Android Studio 的 Emulator，或通过 USB 数据线连接手机（并开启 USB 调试）。")
        sys.exit(1)
        
    target_device = devices[0]
    print_success(f"找到设备: {target_device}，将部署到该设备。")
    
    print_step("正在安装 APK 到设备...")
    install_cmd = f"adb -s {target_device} install -r {apk_path}"
    run_command(install_cmd)
    
    print_step("正在启动应用...")
    package_name = "com.example.myapplication"
    main_activity = f"{package_name}/.MainActivity"
    start_cmd = f"adb -s {target_device} shell am start -n {main_activity}"
    run_command(start_cmd)
    
    print_success("一键启动完成！快去设备上看看吧")

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    try:
        main()
    except KeyboardInterrupt:
        print_error("用户手动终止了程序。")
        sys.exit(1)
