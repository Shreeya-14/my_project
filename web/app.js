const $ = (id) => document.getElementById(id);
let sensors = [];
let selectedSensor = null;

async function api(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) throw new Error("Java request failed");
  return response.json();
}

function moistureColor(value) {
  return value < 35 ? "#db5c54" : value < 60 ? "#efa944" : "#20a56c";
}

function zoneClass(status) {
  if (status === "Irrigate now") return "dry";
  if (status === "Monitor") return "monitor";
  return "healthy";
}

function toast(message) {
  const box = $("toast");
  if (!box) return;
  box.textContent = message;
  box.style.display = "block";
  setTimeout(() => box.style.display = "none", 2600);
}

async function setServerStatus() {
  try {
    await api("/api/health");
    const dot = $("server-dot");
    const text = $("server-text");
    if (dot) dot.style.background = "#20a56c";
    if (text) text.textContent = "Java engine online";
  } catch {
    const dot = $("server-dot");
    const text = $("server-text");
    if (dot) dot.style.background = "#db5c54";
    if (text) text.textContent = "Java engine offline";
  }
}

async function loadSensors() {
  const data = await api("/api/sensors");
  sensors = data.sensors;
}

function drawMap(range = null, highlighted = [], clickPoint = null) {
  const map = $("farm-map");
  if (!map) return;

  map.innerHTML = "";

  for (let i = 0; i <= 100; i += 10) {
    map.innerHTML += `<path d="M${i} 0V100 M0 ${i}H100" stroke="#d5e5bf" stroke-width=".35"/>`;
  }

  if (range) {
    map.innerHTML += `<rect x="${range.xmin}" y="${100 - range.ymax}"
      width="${range.xmax - range.xmin}" height="${range.ymax - range.ymin}"
      fill="#123c32" fill-opacity=".14" stroke="#123c32"
      stroke-width=".7" stroke-dasharray="2 1"/>`;
  }

  if (clickPoint) {
    map.innerHTML += `<path d="M${clickPoint.x - 2} ${100 - clickPoint.y}H${clickPoint.x + 2}
      M${clickPoint.x} ${98 - clickPoint.y}V${102 - clickPoint.y}"
      stroke="#102f29" stroke-width=".8"/>`;
  }

  sensors.forEach(sensor => {
    const selected = selectedSensor && selectedSensor.id === sensor.id;
    const highlightedSensor = highlighted.some(item => item.id === sensor.id);

    map.innerHTML += `<circle cx="${sensor.x}" cy="${100 - sensor.y}"
      r="${selected ? 2.5 : highlightedSensor ? 2.05 : 1.55}"
      fill="${moistureColor(sensor.moisture)}"
      stroke="${selected || highlightedSensor ? "#102f29" : "#fff"}"
      stroke-width="${selected || highlightedSensor ? ".8" : ".45"}">
      <title>${sensor.id}: ${sensor.moisture}% moisture</title>
    </circle>`;

    map.innerHTML += `<text x="${sensor.x + 1.7}" y="${101 - sensor.y}"
      font-size="2.55" fill="#315548">${sensor.id}</text>`;
  });
}

async function dashboardPage() {
  const summary = await api("/api/summary");
  const zoneData = await api("/api/zones");

  $("sensor-count").textContent = summary.sensorCount;
  $("avg-moisture").textContent = summary.averageMoisture.toFixed(1) + "%";
  $("dry-count").textContent = summary.drySensors;
  $("farm-status").textContent = summary.farmStatus;

  $("gauge-value").textContent = summary.averageMoisture.toFixed(0) + "%";
  $("gauge-ring").style.background =
    `conic-gradient(#20a56c ${summary.averageMoisture * 3.6}deg, #e6eee6 0deg)`;

  $("gauge-status").textContent = summary.farmStatus;
  $("gauge-message").textContent =
    `${summary.drySensors} sensor(s) are below the 35% moisture threshold.`;

  $("dashboard-zones").innerHTML = zoneData.zones.map(zone => `
    <div class="mini-zone">
      <div><strong>${zone.name}</strong><small>${zone.sensorCount} sensors</small></div>
      <div><strong>${zone.averageMoisture.toFixed(1)}%</strong><small>${zone.status}</small></div>
    </div>
  `).join("");
}

function showSensorDetails(sensor) {
  const box = $("sensor-detail");
  if (!box) return;

  box.innerHTML = `
    <div class="detail-item"><span>Sensor</span><strong>${sensor.id}</strong></div>
    <div class="detail-item"><span>Location</span><strong>(${sensor.x}, ${sensor.y})</strong></div>
    <div class="detail-item"><span>Moisture</span><strong>${sensor.moisture}%</strong></div>
    <div class="detail-item"><span>pH / Nutrients</span><strong>${sensor.ph} / ${sensor.nutrients}</strong></div>
    <div class="detail-item"><span>Crop health</span><strong>${sensor.cropHealth}</strong></div>
  `;
}

async function fieldPage() {
  await loadSensors();
  drawMap();

  $("farm-map").addEventListener("click", async event => {
    const map = $("farm-map");
    const rect = map.getBoundingClientRect();

    // Correctly maps click location to SVG's 100 × 100 coordinates.
    const scale = Math.min(rect.width / 100, rect.height / 100);
    const offsetX = (rect.width - 100 * scale) / 2;
    const offsetY = (rect.height - 100 * scale) / 2;

    const x = Math.round((event.clientX - rect.left - offsetX) / scale);
    const y = Math.round(100 - (event.clientY - rect.top - offsetY) / scale);

    try {
      const data = await api(`/api/nearest?x=${x}&y=${y}`);
      selectedSensor = data.sensor;
      drawMap(null, [], { x, y });
      showSensorDetails(data.sensor);

      $("nearest-title").textContent = `Nearest: ${data.sensor.id}`;
      $("nearest-output").textContent =
        `${data.distance.toFixed(1)} m away · ${data.sensor.moisture}% moisture · visited ${data.nodesVisited} KD-Tree nodes.`;

      $("query-trace").innerHTML =
        `<strong>KD-Tree:</strong> Java followed alternating x/y split planes toward (${x}, ${y}). It pruned branches that could not contain a closer sensor.`;
    } catch {
      toast("Could not run nearest-sensor search.");
    }
  });

  $("run-range").addEventListener("click", async () => {
    const range = {
      xmin: Number($("xmin").value), xmax: Number($("xmax").value),
      ymin: Number($("ymin").value), ymax: Number($("ymax").value)
    };

    if (range.xmin > range.xmax || range.ymin > range.ymax) {
      $("range-output").textContent = "Minimum values must be smaller than maximum values.";
      return;
    }

    try {
      const data = await api(
        `/api/range?xmin=${range.xmin}&xmax=${range.xmax}&ymin=${range.ymin}&ymax=${range.ymax}`
      );

      selectedSensor = null;
      drawMap(range, data.hits);

      $("range-output").textContent =
        `${data.count} sensors found. ${data.recommendation}`;

      $("query-trace").innerHTML =
        `<strong>2D Orthogonal Range Tree:</strong> Java decomposed the selected X-range into relevant tree subtrees. Each subtree has a secondary Y-sorted catalog, which is searched to retrieve sensors inside the rectangle.`;
    } catch {
      toast("Could not run range query.");
    }
  });

  $("add-sensor").addEventListener("click", async () => {
    try {
      const data = await api("/api/add", { method: "POST" });
      await loadSensors();
      drawMap();
      $("query-trace").textContent =
        `${data.sensor.id} was added. Java rebuilt the KD-Tree, range index, and sorted query lists.`;
      toast("New simulated sensor added.");
    } catch {
      toast("Could not add sensor.");
    }
  });

  $("remove-sensor").addEventListener("click", async () => {
    if (!selectedSensor) {
      toast("Click the map and select a nearest sensor first.");
      return;
    }

    try {
      const data = await api(
        `/api/remove?id=${encodeURIComponent(selectedSensor.id)}`,
        { method: "DELETE" }
      );

      if (!data.removed) {
        toast("Sensor could not be removed.");
        return;
      }

      toast(`${selectedSensor.id} removed.`);
      selectedSensor = null;
      await loadSensors();
      drawMap();
      $("sensor-detail").innerHTML = "<p>The selected sensor was removed.</p>";
    } catch {
      toast("Could not remove sensor.");
    }
  });
}

async function irrigationPage() {
  async function renderZones() {
    const data = await api("/api/zones");

    $("zone-grid").innerHTML = data.zones.map(zone => `
      <article class="zone-card ${zoneClass(zone.status)}">
        <p class="eyebrow">${zone.name.toUpperCase()}</p>
        <h3>${zone.status}</h3>
        <p>Coordinates: x ${zone.xmin}–${zone.xmax}, y ${zone.ymin}–${zone.ymax}</p>
        <div class="zone-values">
          <div><strong>${zone.averageMoisture.toFixed(1)}%</strong><span>average moisture</span></div>
          <div><strong>${zone.drySensorCount}</strong><span>dry sensors</span></div>
          <div><strong>${zone.sensorCount}</strong><span>total sensors</span></div>
        </div>
        <div class="zone-action">${zone.action}</div>
      </article>
    `).join("");
  }

  await renderZones();
  $("refresh-zones").addEventListener("click", renderZones);
}

async function soilPage() {
  const report = await api("/api/fertilizers");
  await loadSensors();

  $("soil-ph").textContent = report.averagePH.toFixed(2);
  $("soil-nutrients").textContent = report.averageNutrients.toFixed(1);
  $("low-nutrients").textContent = report.lowNutrientCount;
  $("fertilizer-status").textContent = report.phStatus;
  $("ph-status").textContent = report.phStatus + " soil";
  $("fertilizer-title").textContent = "Recommended action";
  $("fertilizer-copy").textContent = report.fertilizerSuggestion;
  $("ph-title").textContent = report.phStatus + " pH";
  $("ph-copy").textContent = report.soilMessage;

  $("soil-table").innerHTML = sensors.slice(0, 12).map(sensor => `
    <tr>
      <td><strong>${sensor.id}</strong></td>
      <td>(${sensor.x}, ${sensor.y})</td>
      <td>${sensor.moisture}%</td>
      <td>${sensor.ph}</td>
      <td>${sensor.nutrients}</td>
      <td>${sensor.cropHealth}</td>
    </tr>
  `).join("");
}

async function algorithmsPage() {
  async function runCascade() {
    const moisture = Number($("moisture-slider").value);
    $("target-value").textContent = moisture + "%";

    const data = await api(`/api/fractional?moisture=${moisture}`);

    $("cascade-output").innerHTML = `
      <div class="cascade-result"><span>Main sorted list · Position ${data.mainPosition}</span><strong>${data.mainSensor.id} · ${data.mainSensor.moisture}%</strong></div>
      <div class="cascade-result"><span>North sorted list · Position ${data.northPosition}</span><strong>${data.northSensor.id} · ${data.northSensor.moisture}%</strong></div>
      <div class="cascade-result"><span>South sorted list · Position ${data.southPosition}</span><strong>${data.southSensor.id} · ${data.southSensor.moisture}%</strong></div>
    `;
  }

  $("moisture-slider").addEventListener("input", runCascade);
  await runCascade();
}

async function initialize() {
  await setServerStatus();

  const page = document.body.dataset.page;

  try {
    if (page === "dashboard") await dashboardPage();
    if (page === "field") await fieldPage();
    if (page === "irrigation") await irrigationPage();
    if (page === "soil") await soilPage();
    if (page === "algorithms") await algorithmsPage();
  } catch (error) {
    console.error(error);
    toast("Java server is not responding. Start Main.java first.");
  }
}

initialize();