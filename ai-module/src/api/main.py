"""
FastAPI Main Application
Entry point for the AI Module API server
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import HTMLResponse
import logging
import os
from pathlib import Path
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="AI Module - Visual Search & Recommendations",
    description="Deep learning-powered visual search and AI-based product recommendations for car spare parts",
    version="2.0.0"
)

# Configure CORS
allowed_origins = os.getenv("ALLOWED_ORIGINS", "http://localhost:4200,http://localhost:8080").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Import routers
from .data_collection_routes import router as data_collection_router
from .visual_search_routes import router as visual_search_router
from .recommendation_routes import router as recommendation_router

# Register routers
app.include_router(data_collection_router)
app.include_router(visual_search_router)
app.include_router(recommendation_router)

# Health check endpoint
@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "ai-module",
        "version": "2.0.0",
        "features": ["visual-search", "recommendations"]
    }

# Root endpoint
@app.get("/")
async def root():
    """Root endpoint with API information"""
    return {
        "message": "AI Module API - Visual Search & Recommendations",
        "version": "2.0.0",
        "endpoints": {
            "health": "/health",
            "docs": "/docs",
            "data_collection": "/api/v1/data-collection",
            "visual_search": "/api/v1/visual-search",
            "recommendations": "/api/v1/recommendations"
        }
    }

# Serve admin interface (static HTML)
@app.get("/admin/data-collection", response_class=HTMLResponse)
async def data_collection_interface():
    """Serve data collection admin interface"""
    html_path = Path(__file__).parent.parent.parent / "static" / "data_collection.html"
    
    if html_path.exists():
        with open(html_path, 'r') as f:
            return f.read()
    else:
        return """
        <html>
            <head><title>Data Collection Interface</title></head>
            <body>
                <h1>Data Collection Interface</h1>
                <p>Static HTML file not found. Please create: static/data_collection.html</p>
                <p>Use the API endpoints directly:</p>
                <ul>
                    <li>POST /api/v1/data-collection/upload - Upload images</li>
                    <li>GET /api/v1/data-collection/stats - Get statistics</li>
                    <li>GET /api/v1/data-collection/validate - Validate dataset</li>
                </ul>
                <p><a href="/docs">View API Documentation</a></p>
            </body>
        </html>
        """

if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv("API_HOST", "0.0.0.0")
    port = int(os.getenv("API_PORT", 5000))
    
    logger.info(f"Starting AI Module API server on {host}:{port}")
    
    uvicorn.run(
        "src.api.main:app",
        host=host,
        port=port,
        reload=True,  # Enable auto-reload during development
        log_level="info"
    )
