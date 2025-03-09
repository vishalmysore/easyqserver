import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  const res = http.get('https://easyqz.online');
  sleep(1);
}