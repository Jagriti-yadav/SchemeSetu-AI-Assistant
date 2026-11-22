import { useState } from "react";
import "./App.css";

function App() {
  const [activeTab, setActiveTab] = useState("manual");
  const [file, setFile] = useState(null);
  const [results, setResults] = useState(null);
  const [profile, setProfile] = useState(null);
  const [totalRecommendations, setTotalRecommendations] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [profileForm, setProfileForm] = useState({
    fullName: "",
    age: "",
    gender: "",
    state: "Uttar Pradesh",
    district: "",
    occupation: "",
    annualFamilyIncome: "",
    category: "",
    area: "",
    isFarmer: "false",
    disabilityStatus: "false",
    hasAgriculturalLand: "unknown",
    isBpl: "unknown",
    hasExistingLpgConnection: "unknown",
    ayushmanBeneficiaryStatus: "unknown",
  });

  const resetResults = () => {
    setResults(null);
    setProfile(null);
    setTotalRecommendations(0);
    setError("");
  };

  const handleFormChange = (event) => {
    const { name, value } = event.target;

    setProfileForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const scrollToResults = () => {
    setTimeout(() => {
      document.getElementById("results")?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 100);
  };

  const handleManualSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    resetResults();

    const payload = {
      ...profileForm,
      age: Number(profileForm.age),
      annualFamilyIncome: Number(profileForm.annualFamilyIncome),
      isFarmer: profileForm.isFarmer === "true",
      disabilityStatus: profileForm.disabilityStatus === "true",
    };

    try {
      const response = await fetch(
        "http://localhost:8080/api/recommendations/form",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        }
      );

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(
          data.error || `Request failed (${response.status})`
        );
      }

      if (!Array.isArray(data.recommendations)) {
        throw new Error(
          "Invalid recommendations received from server."
        );
      }

      let extractedProfile = data.profile ?? payload;

      if (typeof extractedProfile === "string") {
        try {
          extractedProfile = JSON.parse(extractedProfile);
        } catch {
          extractedProfile = payload;
        }
      }

      setProfile(extractedProfile);
      setResults(data.recommendations);

      setTotalRecommendations(
        data.totalRecommendations ?? data.recommendations.length
      );

      scrollToResults();
    } catch (err) {
      setError(
        err.message === "Failed to fetch"
          ? "Cannot connect to backend. Please start Spring Boot on port 8080."
          : err.message || "Unable to find schemes."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (event) => {
    const selectedFile = event.target.files?.[0];

    if (
      selectedFile &&
      !selectedFile.name.toLowerCase().endsWith(".pdf")
    ) {
      setFile(null);
      setError("Please select a PDF file.");
      resetResults();
      return;
    }

    setFile(selectedFile || null);
    resetResults();
  };

  const handleAnalyze = async () => {
    if (!file) {
      setError("Please select your profile PDF first.");
      return;
    }

    setLoading(true);
    resetResults();

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(
        "http://localhost:8080/api/recommendations/analyze",
        {
          method: "POST",
          body: formData,
        }
      );

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(
          data.error || `Request failed (${response.status})`
        );
      }

      if (!Array.isArray(data.recommendations)) {
        throw new Error(
          "Invalid recommendations received from server."
        );
      }

      let extractedProfile = data.profile ?? null;

      if (typeof extractedProfile === "string") {
        try {
          extractedProfile = JSON.parse(extractedProfile);
        } catch {
          extractedProfile = null;
        }
      }

      setProfile(extractedProfile);
      setResults(data.recommendations);

      setTotalRecommendations(
        data.totalRecommendations ?? data.recommendations.length
      );

      scrollToResults();
    } catch (err) {
      setError(
        err.message === "Failed to fetch"
          ? "Cannot connect to backend. Please start Spring Boot on port 8080."
          : err.message || "Unable to analyze PDF."
      );
    } finally {
      setLoading(false);
    }
  };

  const formatStatus = (status) => {
    return (status || "NEEDS_MORE_INFORMATION")
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) => letter.toUpperCase());
  };

  const getStatusClass = (status) => {
    const normalized = (status || "").toUpperCase();

    if (normalized === "ELIGIBLE") return "status-eligible";

    if (normalized === "NOT_ELIGIBLE") {
      return "status-not-eligible";
    }

    return "status-needs-info";
  };

  const formatIncome = (income) => {
    if (
      income === null ||
      income === undefined ||
      income === ""
    ) {
      return "Not provided";
    }

    const numericIncome = Number(income);

    return Number.isFinite(numericIncome)
      ? `₹${numericIncome.toLocaleString("en-IN")}`
      : String(income);
  };

  const displayAnswer = (
    value,
    yesText = "Yes",
    noText = "No"
  ) => {
    if (value === true || value === "true") return yesText;

    if (value === false || value === "false") return noText;

    return "Not provided";
  };

  const startAgain = () => {
    resetResults();
    setFile(null);

    const input = document.getElementById("profile-file");

    if (input) input.value = "";

    document.getElementById("find-schemes")?.scrollIntoView({
      behavior: "smooth",
    });
  };

  return (
    <div className="app">

      {/* PREMIUM NAVBAR */}

      <nav className="navbar">

        <a href="#home" className="logo">
          <span className="logo-icon">S</span>

          <span className="logo-text">
            Scheme<span>Setu</span>
          </span>
        </a>

        <div className="nav-links">
          <a href="#home">Home</a>
          <a href="#find-schemes">Find Schemes</a>
          <a href="#how-it-works">How It Works</a>
        </div>

        <a href="#find-schemes" className="nav-button">
          Get Started <span>↗</span>
        </a>

      </nav>

      <main>

        {/* HERO SECTION */}

        <section className="hero" id="home">
          <div className="hero-content">

            <span className="badge">
              ✦ AI-POWERED GOVERNMENT SCHEME DISCOVERY
            </span>

            <h1>
              Government Schemes,
              <br />
              <span>Made for You.</span>
            </h1>

            <p className="hero-description">
              Discover government schemes that match your profile.
              Enter your details or upload a PDF to explore
              potentially relevant government opportunities.
            </p>

            <div className="hero-actions">
              <a href="#find-schemes" className="primary-button">
                Find My Schemes <span>→</span>
              </a>

              <a href="#how-it-works" className="secondary-button">
                How It Works
              </a>
            </div>

            <div className="hero-points">
              <span>✓ Personalized discovery</span>
              <span>✓ Manual form and PDF upload</span>
              <span>✓ Official scheme links</span>
            </div>
          </div>

          {/* HERO VISUAL */}

          <div className="hero-visual">
            <div className="visual-card">

              <div className="visual-top">
                <div>
                  <span className="small-label">
                    SCHEME DISCOVERY
                  </span>

                  <h3>Your opportunities</h3>
                </div>

                <span className="sparkle">✦</span>
              </div>

              <div className="visual-item">
                <div className="scheme-icon green">₹</div>

                <div>
                  <h4>Financial Support</h4>
                  <p>Explore assistance programs</p>
                </div>

                <span className="item-arrow">↗</span>
              </div>

              <div className="visual-item">
                <div className="scheme-icon yellow">⌂</div>

                <div>
                  <h4>Housing Benefits</h4>
                  <p>Discover housing schemes</p>
                </div>

                <span className="item-arrow">↗</span>
              </div>

              <div className="visual-item">
                <div className="scheme-icon blue">✚</div>

                <div>
                  <h4>Healthcare Support</h4>
                  <p>Explore health coverage</p>
                </div>

                <span className="item-arrow">↗</span>
              </div>

              <div className="visual-note">
                <span>✦</span>
                Personalized scheme discovery
              </div>

            </div>

            <div className="floating-dot dot-one">✦</div>
            <div className="floating-dot dot-two">✓</div>
          </div>
        </section>

        {/* STATS */}

        <section className="stats">
          <div>
            <strong>01</strong>
            <span>Enter your details</span>
          </div>

          <div>
            <strong>02</strong>
            <span>Profile is analyzed</span>
          </div>

          <div>
            <strong>03</strong>
            <span>Explore schemes</span>
          </div>
        </section>

        {/* FIND SCHEMES */}

        <section className="upload-section" id="find-schemes">

          <div className="section-heading">
            <span className="section-label">GET STARTED</span>

            <h2>Find schemes made for you</h2>

            <p>
              Enter your personal details or upload your profile PDF
              to discover potentially relevant government schemes.
            </p>
          </div>

          {/* TABS */}

          <div className="form-tabs">
            <button
              type="button"
              className={
                activeTab === "manual"
                  ? "tab active-tab"
                  : "tab"
              }
              onClick={() => {
                setActiveTab("manual");
                resetResults();
              }}
            >
              Fill Details Manually
            </button>

            <button
              type="button"
              className={
                activeTab === "pdf"
                  ? "tab active-tab"
                  : "tab"
              }
              onClick={() => {
                setActiveTab("pdf");
                resetResults();
              }}
            >
              Upload Profile PDF
            </button>
          </div>

          {/* MANUAL FORM */}

          {activeTab === "manual" && (
            <form
              className="upload-card manual-form"
              onSubmit={handleManualSubmit}
            >
              <div className="upload-icon">✎</div>

              <h3>Personal Information</h3>

              <p>
                Fill in your details to find government schemes
                that may match your profile.
              </p>

              <div className="form-grid">

                <div className="form-field">
                  <label>Full Name *</label>

                  <input
                    type="text"
                    name="fullName"
                    placeholder="Enter your full name"
                    value={profileForm.fullName}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Age *</label>

                  <input
                    type="number"
                    name="age"
                    placeholder="Enter your age"
                    min="1"
                    max="120"
                    value={profileForm.age}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Gender *</label>

                  <select
                    name="gender"
                    value={profileForm.gender}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select gender</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>State *</label>

                  <select
                    name="state"
                    value={profileForm.state}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select state</option>

                    {[
                      "Andhra Pradesh",
                      "Arunachal Pradesh",
                      "Assam",
                      "Bihar",
                      "Chhattisgarh",
                      "Goa",
                      "Gujarat",
                      "Haryana",
                      "Himachal Pradesh",
                      "Jharkhand",
                      "Karnataka",
                      "Kerala",
                      "Madhya Pradesh",
                      "Maharashtra",
                      "Manipur",
                      "Meghalaya",
                      "Mizoram",
                      "Nagaland",
                      "Odisha",
                      "Punjab",
                      "Rajasthan",
                      "Sikkim",
                      "Tamil Nadu",
                      "Telangana",
                      "Tripura",
                      "Uttar Pradesh",
                      "Uttarakhand",
                      "West Bengal",
                      "Delhi",
                    ].map((state) => (
                      <option key={state} value={state}>
                        {state}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-field">
                  <label>District *</label>

                  <input
                    type="text"
                    name="district"
                    placeholder="Enter your district"
                    value={profileForm.district}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Occupation *</label>

                  <select
                    name="occupation"
                    value={profileForm.occupation}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select occupation</option>
                    <option value="Farmer">Farmer</option>
                    <option value="Student">Student</option>
                    <option value="Self Employed">Self Employed</option>
                    <option value="Private Employee">
                      Private Employee
                    </option>
                    <option value="Government Employee">
                      Government Employee
                    </option>
                    <option value="Unemployed">Unemployed</option>
                    <option value="Business">Business</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Annual Family Income (₹) *</label>

                  <input
                    type="number"
                    name="annualFamilyIncome"
                    placeholder="Example: 150000"
                    min="0"
                    value={profileForm.annualFamilyIncome}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-field">
                  <label>Category *</label>

                  <select
                    name="category"
                    value={profileForm.category}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select category</option>
                    <option value="General">General</option>
                    <option value="OBC">OBC</option>
                    <option value="SC">SC</option>
                    <option value="ST">ST</option>
                    <option value="EWS">EWS</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Area Type *</label>

                  <select
                    name="area"
                    value={profileForm.area}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select area</option>
                    <option value="Rural">Rural</option>
                    <option value="Urban">Urban</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Are you a farmer? *</label>

                  <select
                    name="isFarmer"
                    value={profileForm.isFarmer}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="false">No</option>
                    <option value="true">Yes</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>Person with disability? *</label>

                  <select
                    name="disabilityStatus"
                    value={profileForm.disabilityStatus}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="false">No</option>
                    <option value="true">Yes</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>
                    Do you or your family own agricultural land?
                  </label>

                  <select
                    name="hasAgriculturalLand"
                    value={profileForm.hasAgriculturalLand}
                    onChange={handleFormChange}
                  >
                    <option value="unknown">
                      Not sure / Not provided
                    </option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>
                    Is your household in the eligible poor/BPL category?
                  </label>

                  <select
                    name="isBpl"
                    value={profileForm.isBpl}
                    onChange={handleFormChange}
                  >
                    <option value="unknown">
                      Not sure / Not provided
                    </option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>
                    Does your household already have an LPG connection?
                  </label>

                  <select
                    name="hasExistingLpgConnection"
                    value={profileForm.hasExistingLpgConnection}
                    onChange={handleFormChange}
                  >
                    <option value="unknown">
                      Not sure / Not provided
                    </option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>

                <div className="form-field">
                  <label>
                    Are you listed as an Ayushman Bharat PM-JAY beneficiary?
                  </label>

                  <select
                    name="ayushmanBeneficiaryStatus"
                    value={profileForm.ayushmanBeneficiaryStatus}
                    onChange={handleFormChange}
                  >
                    <option value="unknown">
                      Not checked / Not sure
                    </option>
                    <option value="true">
                      Yes, I have verified
                    </option>
                    <option value="false">
                      No / Not listed
                    </option>
                  </select>
                </div>

              </div>

              <button
                type="submit"
                className="analyze-button"
                disabled={loading}
              >
                {loading
                  ? "Finding Your Schemes..."
                  : "Find My Schemes →"}
              </button>

              <p className="privacy-note">
                Your details will be used to evaluate available scheme criteria.
              </p>

              {error && (
                <div className="error-message" role="alert">
                  {error}
                </div>
              )}
            </form>
          )}

          {/* PDF UPLOAD */}

          {activeTab === "pdf" && (
            <div className="upload-card">

              <div className="upload-icon">↑</div>

              <h3>Upload your profile PDF</h3>

              <p>
                Select a PDF containing your personal details,
                income, occupation and other relevant information.
              </p>

              <label
                className="file-input-label"
                htmlFor="profile-file"
              >
                Choose PDF
              </label>

              <input
                id="profile-file"
                type="file"
                accept=".pdf,application/pdf"
                onChange={handleFileChange}
              />

              {file && (
                <div className="selected-file">
                  <span>📄</span>
                  <span>{file.name}</span>
                  <span>
                    {(file.size / 1024).toFixed(1)} KB
                  </span>
                </div>
              )}

              <button
                className="analyze-button"
                onClick={handleAnalyze}
                disabled={loading || !file}
              >
                {loading
                  ? "Analyzing Your Profile..."
                  : "Analyze My Profile →"}
              </button>

              <p className="privacy-note">
                Upload a readable PDF containing relevant profile information.
              </p>

              {error && (
                <div className="error-message" role="alert">
                  {error}
                </div>
              )}

            </div>
          )}

        </section>

        {/* RESULTS */}

        {results !== null && (
          <section className="results-section" id="results">

            <div className="section-heading">
              <span className="section-label">
                YOUR PERSONALIZED REPORT
              </span>

              <h2>Your Government Scheme Recommendations</h2>

              <p>
                Review schemes evaluated using your profile.
                Final eligibility is determined by the relevant government authority.
              </p>
            </div>

            {/* PROFILE CARD */}

            {profile && (
              <div className="result-card profile-card">

                <span className="result-number">
                  APPLICANT PROFILE
                </span>

                <h3>{profile.fullName || "Applicant"}</h3>

                <div className="profile-grid">

                  <div>
                    <strong>Age</strong>
                    <p>{profile.age ?? "Not provided"}</p>
                  </div>

                  <div>
                    <strong>Gender</strong>
                    <p>{profile.gender || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>State</strong>
                    <p>{profile.state || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>District</strong>
                    <p>{profile.district || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>Occupation</strong>
                    <p>{profile.occupation || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>Annual Family Income</strong>
                    <p>{formatIncome(profile.annualFamilyIncome)}</p>
                  </div>

                  <div>
                    <strong>Category</strong>
                    <p>{profile.category || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>Area</strong>
                    <p>{profile.area || "Not provided"}</p>
                  </div>

                  <div>
                    <strong>Farmer</strong>
                    <p>{displayAnswer(profile.isFarmer)}</p>
                  </div>

                  <div>
                    <strong>Disability Status</strong>
                    <p>{displayAnswer(profile.disabilityStatus)}</p>
                  </div>

                  <div>
                    <strong>Agricultural Land</strong>
                    <p>{displayAnswer(profile.hasAgriculturalLand)}</p>
                  </div>

                  <div>
                    <strong>BPL / Eligible Poor Household</strong>
                    <p>{displayAnswer(profile.isBpl)}</p>
                  </div>

                  <div>
                    <strong>Existing LPG Connection</strong>
                    <p>
                      {displayAnswer(
                        profile.hasExistingLpgConnection
                      )}
                    </p>
                  </div>

                  <div>
                    <strong>Ayushman Beneficiary Status</strong>
                    <p>
                      {displayAnswer(
                        profile.ayushmanBeneficiaryStatus,
                        "Self-reported: Verified",
                        "Self-reported: Not listed"
                      )}
                    </p>
                  </div>

                </div>
              </div>
            )}

            {/* RECOMMENDATION COUNT */}

            <div className="recommendation-summary">
              <strong>{totalRecommendations}</strong>

              <span>
                Potentially relevant schemes identified
              </span>
            </div>

            {/* SCHEME CARDS */}

            {results.length === 0 ? (
              <div className="result-card">
                <h3>No recommendations available</h3>

                <p>
                  No schemes were returned for this profile.
                  Please check the information and try again.
                </p>
              </div>
            ) : (
              <div className="results-grid">

                {results.map((scheme, index) => (
                  <article className="result-card" key={index}>

                    <span className="result-number">
                      SCHEME {String(index + 1).padStart(2, "0")}
                    </span>

                    <h3>
                      {scheme.schemeName || "Government Scheme"}
                    </h3>

                    <p>
                      {scheme.description ||
                        "Please review the official scheme information."}
                    </p>

                    <div
                      className={`status-badge ${getStatusClass(
                        scheme.status
                      )}`}
                    >
                      {formatStatus(scheme.status)}
                    </div>

                    {scheme.reason && (
                      <div className="result-detail">
                        <strong>Why this recommendation?</strong>
                        <p>{scheme.reason}</p>
                      </div>
                    )}

                    <div className="result-detail">
                      <strong>Benefits</strong>

                      <p>
                        {scheme.benefits ||
                          "Refer to the official scheme website."}
                      </p>
                    </div>

                    <div className="result-detail">
                      <strong>Eligibility Criteria</strong>

                      <p>
                        {scheme.eligibility ||
                          "Please verify the official eligibility criteria."}
                      </p>
                    </div>

                    {(scheme.missingInformation ||
                      scheme.missing_information) && (
                      <div className="result-detail">
                        <strong>
                          Additional Information Required
                        </strong>

                        <p>
                          {Array.isArray(
                            scheme.missingInformation ||
                              scheme.missing_information
                          )
                            ? (
                                scheme.missingInformation ||
                                scheme.missing_information
                              ).join(", ")
                            : scheme.missingInformation ||
                              scheme.missing_information}
                        </p>
                      </div>
                    )}

                    {scheme.nextStep && (
                      <div className="result-detail">
                        <strong>Recommended Next Step</strong>
                        <p>{scheme.nextStep}</p>
                      </div>
                    )}

                    {scheme.notice && (
                      <div className="result-detail">
                        <p>{scheme.notice}</p>
                      </div>
                    )}

                    {scheme.officialWebsite && (
                      <a
                        className="official-link"
                        href={scheme.officialWebsite}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Visit Official Website ↗
                      </a>
                    )}

                  </article>
                ))}

              </div>
            )}

            <p className="disclaimer">
              These recommendations are preliminary and do not guarantee
              eligibility or approval. Confirm the latest rules with the
              relevant government department before applying.
            </p>

            <div className="report-actions">
              <button
                className="secondary-button"
                onClick={startAgain}
              >
                Start Again ↻
              </button>
            </div>

          </section>
        )}

        {/* HOW IT WORKS */}

        <section className="how-section" id="how-it-works">

          <div className="section-heading">
            <span className="section-label">THE PROCESS</span>

            <h2>Three simple steps</h2>

            <p>
              From your profile to potentially relevant schemes.
            </p>
          </div>

          <div className="steps-grid">

            <div className="step-card">
              <span className="step-number">01</span>

              <h3>Enter Your Details</h3>

              <p>
                Fill in your personal and family information
                or upload a profile PDF.
              </p>
            </div>

            <div className="step-card">
              <span className="step-number">02</span>

              <h3>Profile Analysis</h3>

              <p>
                The backend evaluates your profile against
                available government scheme information.
              </p>
            </div>

            <div className="step-card">
              <span className="step-number">03</span>

              <h3>Explore Recommendations</h3>

              <p>
                Review scheme benefits, eligibility details
                and official links.
              </p>
            </div>

          </div>
        </section>

      </main>

      {/* PREMIUM FOOTER */}

      <footer className="footer" id="footer">

        <div className="footer-main">

          <div className="footer-brand">

            <a href="#home" className="footer-logo">
              <span className="footer-logo-icon">S</span>
              <span>SchemeSetu</span>
            </a>

            <p>
              Making government scheme discovery simpler, smarter
              and more accessible for every citizen.
            </p>

            <div className="footer-tagline">
              ✦ Empowering citizens through technology
            </div>

          </div>

          <div className="footer-column">
            <h4>Quick Links</h4>

            <a href="#home">Home</a>
            <a href="#find-schemes">Find Schemes</a>
            <a href="#how-it-works">How It Works</a>
          </div>

          <div className="footer-column">
            <h4>Explore</h4>

            <a href="#find-schemes">Manual Registration</a>
            <a href="#find-schemes">Upload Profile PDF</a>
            <a href="#results">Recommendations</a>
          </div>

          <div className="footer-column footer-help">
            <h4>Our Purpose</h4>

            <p>
              Helping people discover potentially relevant
              government welfare schemes through AI-assisted
              profile analysis.
            </p>

            <span className="footer-status">
              <span></span>
              Built for Digital India
            </span>
          </div>

        </div>

        <div className="footer-bottom">

          <p>
            © 2026 SchemeSetu. All rights reserved.
          </p>

          <p>
            Made with <span className="heart">♥</span> in India
          </p>

          <a href="#home">
            Back to Top ↑
          </a>

        </div>

      </footer>

    </div>
  );
}

export default App;