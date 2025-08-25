const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 5173;
const API_BASE = process.env.API_BASE || 'http://localhost:8081/api';

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.get('/', (req, res) => {
  res.render('index', { token: '', products: null, error: null, email: '', password: '' });
});

app.post('/login', async (req, res) => {
  const { email, password } = req.body;
  try {
    const resp = await axios.post(`${API_BASE}/auth/signin`, { email, password });
    const token = resp.data.data.tokens.access_token;
    res.render('index', { token, products: null, error: null, email, password });
  } catch (err) {
    res.render('index', { token: '', products: null, error: err.response?.data || err.message, email, password });
  }
});

app.post('/products', async (req, res) => {
  const { token, email, password, page, limit, name, minPrice, maxPrice } = req.body;
  const params = {};
  if (page) params.page = page;
  if (limit) params.limit = limit;
  if (name) params.name = name;
  if (minPrice) params.minPrice = minPrice;
  if (maxPrice) params.maxPrice = maxPrice;
  try {
    const resp = await axios.get(`${API_BASE}/products`, {
      params,
      headers: { Authorization: `Bearer ${token}` },
    });
    res.render('index', { token, products: resp.data, error: null, email, password });
  } catch (err) {
    res.render('index', { token, products: null, error: err.response?.data || err.message, email, password });
  }
});

app.listen(PORT, () => {
  console.log(`CORS test app running at http://localhost:${PORT}`);
});
