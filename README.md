# jquants-java

## Run the sample

```
JQUANTS_MAIL_ADDRESS=<MAIL>
JQUANTS_PASSWORD=<PASSWORD>
# Override only if you host an alternative base URL
# JQUANTS_BASE_URL=https://api.jquants.com/v1
./mvnw exec:java -Dexec.mainClass=io.github.shigaichi.jquants.App
```

The default base URL is `https://api.jquants.com/v1` (non-Pro). Set `JQUANTS_BASE_URL` only if you need to target a different host/version.

If `/token/auth_user` returns only `refreshToken` (e.g. v1 endpoint), the client automatically calls `/token/auth_refresh` to obtain the `idToken`.

https://jpx.gitbook.io/j-quants-ja/api-reference/refreshtoken
