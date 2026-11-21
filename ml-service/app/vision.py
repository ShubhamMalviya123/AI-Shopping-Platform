"""Image search baseline: colour-histogram features + dominant colour (Pillow + NumPy).
Upgrade path: replace features() with CNN / Vision Transformer embeddings and search with FAISS."""
import math
import numpy as np
from PIL import Image
from .seed import PRODUCT_COLORS

def features(img: Image.Image) -> np.ndarray:
    a = np.asarray(img.convert("RGB").resize((64, 64)), dtype=np.int32) // 64
    idx = a[..., 0] * 16 + a[..., 1] * 4 + a[..., 2]
    h = np.bincount(idx.ravel(), minlength=64).astype(float)
    return h / h.sum()

def dominant_rgb(img: Image.Image) -> tuple:
    a = np.asarray(img.convert("RGB").resize((64, 64)), dtype=np.int32)
    q = a // 64
    idx = q[..., 0] * 16 + q[..., 1] * 4 + q[..., 2]
    top = np.bincount(idx.ravel(), minlength=64).argmax()
    return tuple(int(v) for v in a[idx == top].mean(axis=0))

def swatch(rgb: tuple) -> Image.Image:
    """Stand-in product thumbnail. With real data, load the marketplace product image instead."""
    rng = np.random.default_rng(sum(rgb))
    arr = np.clip(np.array(rgb, dtype=float) + rng.normal(0, 18, (64, 64, 3)), 0, 255).astype("uint8")
    return Image.fromarray(arr)

def product_color(listing_id: str) -> tuple:
    return PRODUCT_COLORS.get(listing_id.rsplit("-", 1)[0], (128, 128, 128))

def similarity(query_hist: np.ndarray, dominant: tuple, rgb: tuple) -> float:
    ph = features(swatch(rgb))
    cos = float(query_hist @ ph / (np.linalg.norm(query_hist) * np.linalg.norm(ph) + 1e-9))
    dist = math.sqrt(sum((a - b) ** 2 for a, b in zip(dominant, rgb)))
    return 0.5 * cos + 0.5 * math.exp(-dist / 90)
