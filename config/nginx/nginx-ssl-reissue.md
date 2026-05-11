# Nginx SSL 재발급 가이드

## 대상 도메인

- `mars-pli.kro.kr` — Spring Boot API (외부 포트 26443)
- `minio.mars-pli.kro.kr` — MinIO API(9000) / UI(9001)

> kro.kr DNS는 80 포트 포워딩 제약으로 **DNS-01 방식**으로 발급.

---

## 1. 기존 설정 백업 & 정리

```bash
sudo cp -r /etc/letsencrypt /etc/letsencrypt.bak.$(date +%Y%m%d)
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak.$(date +%Y%m%d)

# 더 이상 사용하지 않는 구 인증서 제거(선택)
sudo certbot delete --cert-name app.mars-crew.shop
sudo certbot delete --cert-name web.mars-crew.shop
sudo certbot delete --cert-name api.mars-crew.shop
sudo certbot delete --cert-name file.mars-crew.shop
```

---

## 2. 인증서 발급 (DNS-01)

```bash
sudo certbot certonly --manual --preferred-challenges dns -d mars-pli.kro.kr
sudo certbot certonly --manual --preferred-challenges dns -d minio.mars-pli.kro.kr
```

certbot이 출력하는 TXT 값을 **kro.kr DNS 관리 화면**에 추가:

| 도메인 | 호스트 입력값 | 값 |
|---|---|---|
| mars-pli.kro.kr | `_acme-challenge` | certbot 출력값 |
| minio.mars-pli.kro.kr | `_acme-challenge.minio` | certbot 출력값 |

전파 확인 후 Enter:

```bash
dig TXT _acme-challenge.mars-pli.kro.kr +short
dig TXT _acme-challenge.minio.mars-pli.kro.kr +short
```

발급 결과:

- `/etc/letsencrypt/live/mars-pli.kro.kr/{fullchain,privkey}.pem`
- `/etc/letsencrypt/live/minio.mars-pli.kro.kr/{fullchain,privkey}.pem`

---

## 3. nginx.conf 교체 & 적용

```bash
sudo cp config/nginx/nginx.conf /etc/nginx/nginx.conf
sudo nginx -t
sudo systemctl reload nginx
```

---

## 4. 접속 확인

```bash
curl -I https://mars-pli.kro.kr:26443/swagger-ui.html
curl -I https://minio.mars-pli.kro.kr:9000/minio/health/ready
```

브라우저: `https://minio.mars-pli.kro.kr:9001/minio/ui/`

---

## 5. 갱신

DNS-01 수동 방식은 **자동 갱신 불가**. 만료 전(60~80일) 2장 절차 반복.
