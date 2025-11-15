document.getElementById('btn').onclick = async function() {
  const url = document.getElementById('url').value.trim();
  if (!url) { alert('Informe uma URL'); return; }
  const res = document.getElementById('result');
  res.textContent = 'Validando...';
  try {
    const r = await fetch('/api/certs/validate?url=' + encodeURIComponent(url));
    if (!r.ok) throw new Error('Erro na requisição');
    const j = await r.json();
    if (j.trusted) {
      res.innerHTML = '<span class="ok">✅ Link confiável: ' + j.url + '</span>';
    } else {
      res.innerHTML = '<span class="bad">⚠️ Possível golpe: ' + j.url + '</span>';
    }
  } catch (e) {
    res.innerHTML = '<span class="bad">Erro: ' + e.message + '</span>';
  }
};
