package br.com.roggersilver.xiaomidebloater;

// Only audited package names and fixed pm arguments are allowed by the server.
interface IPackageControl {
    void destroy() = 16777114;
    int backendUid() = 1;
    String setEnabledState(String packageName, boolean enable) = 2;
}
