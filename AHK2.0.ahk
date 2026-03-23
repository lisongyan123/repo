; ========== AutoHotkey Script Configuration ==========
#SingleInstance Force
SendMode "Input"
SetWorkingDir A_ScriptDir
CoordMode "ToolTip", "Screen"

; ========== Site Configuration ==========
; 定义网站名称、URL 和对应的图标（使用 Unicode 表情符号）
; 可以通过添加 `group: "组名"` 来对网站进行分组
sites := [
    ; 搜索与知识组
    {name: "🔍 Google",    url: "https://www.google.com", group: "搜索与知识"},
    {name: "❓ 知乎",      url: "https://www.zhihu.com", group: "搜索与知识"},

    ; 开发与技术组
    {name: "🐱 GitHub",    url: "https://github.com", group: "开发与技术"},
    {name: "▶️ YouTube",   url: "https://www.youtube.com", group: "开发与技术"},
    
    ; 娱乐组
    {name: "🎬 Bilibili",  url: "https://www.bilibili.com", group: "娱乐"},
]

; ========== 创建主菜单 ==========
MainMenu := Menu()

; ========== 创建网站子菜单（按组分类） ==========
siteMenus := Map() ; 存储各组的子菜单

for site in sites
{
    groupName := site.group ? site.group : "默认"
    name := site.name
    url := site.url

    if (!siteMenus.Has(groupName)) {
        siteMenus[groupName] := Menu()
    }

    ; ✅ 关键修复：通过高阶函数固化 url 值，避免闭包共享变量
    siteMenus[groupName].Add(name, ((u) => (*) => Run(u))(url))
}

; 将所有分组子菜单添加到主菜单
for groupName, groupMenu in siteMenus {
    MainMenu.Add("🌐 " . groupName, groupMenu)
}

; 分隔线
MainMenu.Add()

; 其他功能
MainMenu.Add("🔄 刷新脚本", (*) => ReloadScript())
MainMenu.Add("❌ 退出脚本", (*) => ExitApp())

; ========== 热键 ==========
MButton::
{
    MouseGetPos(&x, &y)
    MainMenu.Show(x, y)
}

^!m:: MainMenu.Show()

; ========== 函数 ==========
ReloadScript()
{
    try
    {
        Reload()
        Sleep 1000
        if !WinExist("ahk_class AutoHotkey")
        {
            ToolTip("⚠️ 脚本重载失败", 100, 100)
            SetTimer(() => ToolTip(), -2000)
        }
    }
    catch as e
    {
        MsgBox("重载失败: " e.Message, "错误", "IconX")
    }
}

ExitApp()
{
    ExitApp()
}

; ========== 启动提示 ==========
ToolTip("✨ 网站快捷菜单已启动！`n按鼠标中键打开", 100, 100)
SetTimer(() => ToolTip(), -3000)

; ========== 托盘菜单 ==========
A_TrayMenu.Delete()
A_TrayMenu.Add("🌐 打开主菜单", (*) => MainMenu.Show())
A_TrayMenu.Add()
A_TrayMenu.Add("🔄 重新加载", (*) => ReloadScript())
A_TrayMenu.Add("❌ 退出", (*) => ExitApp())
A_IconTip := "网站快捷菜单`n中键打开 | 右键托盘图标"
